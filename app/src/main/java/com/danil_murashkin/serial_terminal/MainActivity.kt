@file:Suppress("DEPRECATION")

package com.danil_murashkin.serial_terminal

import android.R
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.danil_murashkin.serial_terminal.databinding.ActivityMainBinding
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    ///?  Saving EditText and retrieve it automatically
    /// https://www.digitalocean.com/community/tutorials/android-shared-preferences-example-tutorial
    // https://gist.github.com/s3va/85c1c330f9786c60903934d7e3e8f479

    private val TAG:String = "SerialTerminal"
    private lateinit var binding: ActivityMainBinding

    private val uart = UARTPort()
    private val readUartTask = mainWhileReadUartAsyncTask()
    private var uartConnectedFlag:Boolean = false
    private var uartPortName:String = "/dev/tty1WK2"
    private var uartPortBaudrate:Int = 460800
    private var readUartTaskWorking:Boolean = false
    private val baseCharset:Charset = Charsets.US_ASCII

    private var hexModeFlag:Boolean = false
    private var fileUri:Uri = Uri.parse( "content://com.android.providers.downloads.documents/document/msf%3A55" )
    //private val sendFileUri:Uri = Uri.parse( "content://com.android.providers.downloads.documents/document/msf%3A56" ) // Test.txt
    private lateinit var fileData:ByteArray
    private var fileChunkSize:Int = 4410
    private var fileUartSendContinueFlag:Boolean = false
    private var fileUartSendChunkId:Int = 0
    private var fileUartSendTime:Int = 0
    private val fileOpenActivity = registerForActivityResult( ActivityResultContracts.OpenDocument() ) { uri ->  if (uri != null) { fileOpen(uri) } }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example of a call to a native method
        binding.statusTextView.text = "Ready for connection"
        binding.consoleTextView.text = ""
        binding.uartPortBaudrateEditText.setText( uartPortBaudrate.toString() )
        binding.fileChunkSizeEditText.setText( fileChunkSize.toString() )
        binding.packetData1EditText.setText("BUS+AUDIO=")
        binding.packetData2EditText.setText("BUS+STATION=30A")
        binding.packetData3EditText.setText("BUS+STATION=30B")
        binding.sendFileButton.isEnabled = false
        binding.uartConnectButton.isEnabled = false

        binding.hexModeButton.setOnClickListener{ hexModeToggle() }
        binding.clearButton.setOnClickListener{ binding.consoleTextView.text = "" }
        binding.uartConnectButton.setOnClickListener{ uartConnect( binding.uartPortsSpinner.adapter.getItem( binding.uartPortsSpinner.selectedItemPosition ).toString(), binding.uartPortBaudrateEditText.text.toString().toInt() ) }
        binding.sendPacket1Button.setOnClickListener{ uartSendText( binding.packetData1EditText.text.toString() ) }
        binding.sendPacket2Button.setOnClickListener{ uartSendText( binding.packetData2EditText.text.toString() ) }
        binding.sendPacket3Button.setOnClickListener{ uartSendText( binding.packetData3EditText.text.toString() ) }
        binding.openFileButton.setOnClickListener{ fileOpenActivity.launch( arrayOf("*/*") ) } // "audio/*", "text/plain"
        binding.sendFileButton.setOnClickListener { uartFileSend( fileData, fileChunkSize ) }

        uartUpdatePortsAtSpinner()
        uartConnect( binding.uartPortsSpinner.adapter.getItem( binding.uartPortsSpinner.selectedItemPosition ).toString(), binding.uartPortBaudrateEditText.text.toString().toInt() )
        fileOpen( fileUri )
        uartFileSend( fileData, fileChunkSize )
        //hexModeToggle()
    }//.onCreate()
    


    inner class mainWhileReadUartAsyncTask: AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String? {
            try {
                TimeUnit.MICROSECONDS.sleep(100)
                while (true) {
                    //TimeUnit.MICROSECONDS.sleep(10)
                    if( uartConnectedFlag ) {
                        val bytes = uart.read()
                        if (bytes != null) {
                            if (hexModeFlag) {
                                Log.d(TAG, "Uart receive bytes: " + HexUtils.bytesToHexString(bytes))
                                statusTextViewAddText("> "+HexUtils.bytesToHexString(bytes)+"\n")
                            } else {
                                Log.d(TAG, "Uart receive text: " + bytes.toString(baseCharset))
                                statusTextViewAddText("> "+bytes.toString(baseCharset)+"\n")
                            }
                        }
                    }
                }
            } finally { }
            readUartTaskWorking = false
            return null
        }//.doInBackground()
        override fun onPostExecute(result: String?) {
            //super.onPostExecute(result)
            readUartTaskWorking = false
        }
    }//.mainWhileReadUartAsyncTask()

    private fun uartFileSend( data:ByteArray, chunkSize:Int ) {
        fileData = data
        fileChunkSize = chunkSize
        fileUartSendChunkId = 0
        fileUartSendTime = 0

        var dataLen:Int = chunkSize
        if( data.size <= chunkSize ) {
            dataLen = data.size
            fileUartSendContinueFlag = false
        } else {
            fileUartSendContinueFlag = true
        }
        uart.write(data.slice( 0..dataLen).toByteArray(), dataLen)
        fileUartSendChunkId++

        if (hexModeFlag) {
            val shortDataStrHex: String = HexUtils.bytesToHexString(data.slice(0..3).toByteArray()) + "..." + HexUtils.bytesToHexString(data.slice(dataLen-4..dataLen-1).toByteArray())
            Log.d(TAG, "Uart send file chunk bytes #$fileUartSendChunkId: len=$dataLen, data=$shortDataStrHex")
            statusTextViewAddText("< $shortDataStrHex \n")
        } else {
            val shortDataStrText: String = data.decodeToString(0,4) + "..." + data.decodeToString(dataLen-4, dataLen-1)
            Log.d(TAG, "Uart send file chunk text #$fileUartSendChunkId: len=$dataLen, data=$shortDataStrText")
            statusTextViewAddText("< $shortDataStrText \n")
        }
    }//.uartFileSend()

    private fun uartSendText(text:String) {
        var sendTextFlag:Boolean = true
        if( uartConnectedFlag ) {
            if( hexModeFlag ) {
                val hexBytes = HexUtils.hexStringToByteArray(text)
                if (hexBytes != null) {
                    sendTextFlag = false
                    uart.write(hexBytes, hexBytes.size)
                    Log.d(TAG, "Uart send bytes: $hexBytes")
                    statusTextViewAddText("< "+HexUtils.bytesToHexString(hexBytes)+"\n")
                }
            }
        }
        if( sendTextFlag ) {
            val textBytes = text.toByteArray(baseCharset)
            uart.write(textBytes, textBytes.size)
            Log.d(TAG, "Uart send text: $text")
            statusTextViewAddText("< $text \n")
        }
    }//.uartSendText()

    private fun uartConnect( portName:String, baudRate:Int ) {
        if(!uartConnectedFlag) {
            val TRUE:Int = 1
            if( uart.open( portName, baudRate ) == TRUE )
            {
                uartPortName = portName
                uartPortBaudrate = baudRate
                uartConnectedFlag = true
                if( !readUartTaskWorking ){
                    readUartTask.execute()
                    readUartTaskWorking = true
                }
                binding.statusTextView.text = "Connected to: $uartPortName"
                binding.uartConnectButton.text = "DISCONNECT"
            } else {
                uart.close();
                binding.statusTextView.text = "Failed connection at: $uartPortName"
            }
        } else {
            binding.statusTextView.text =  "Ready for connection"
            uartConnectedFlag = false
            binding.uartConnectButton.text = "CONNECT"
            uart.close();
        }
    }//.uartConnectButtonClick()

    private fun uartUpdatePortsAtSpinner() {
        val uartPorts: MutableList<String> = ArrayList()
        uartPorts.addAll( uart.getAvailablePorts() )
        if ( uartPorts.size > 0) {
            binding.uartConnectButton.isEnabled = true
        } else {
            binding.statusTextView.text = "Serial ports not found"
            uartPorts.add( "Serial not found" )
            binding.uartConnectButton.isEnabled = false
        }
        val dataAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, uartPorts)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.uartPortsSpinner.adapter = dataAdapter
        uartPorts.forEachIndexed { index, portName ->
            if (portName == uartPortName) {
                Log.d(TAG, "Default port $uartPortName founded")
                binding.uartPortsSpinner.setSelection(index)
            }
        }
    }//.uartUpdatePortsAtSpinner()

    private fun hexModeGetText(text:String): String? {
        val text1Bytes = HexUtils.hexStringToByteArray( text )
        if( text1Bytes != null ){
            if( hexModeFlag ) {
                return HexUtils.bytesToHexString(text1Bytes)
            } else {
                return text1Bytes.decodeToString()
            }
        } else {
            if( hexModeFlag ) {
                val textByteArray = text.toByteArray(baseCharset)
                return HexUtils.bytesToHexString(textByteArray)
            }else {
                return text
            }
        }
        return null
    }
    private fun hexModeToggle() {
        if( !hexModeFlag )
        {
            hexModeFlag = true
            binding.hexModeButton.text = "ASCII"
        } else {
            hexModeFlag = false
            binding.hexModeButton.text = "HEX"
        }
        binding.packetData1EditText.setText( hexModeGetText( binding.packetData1EditText.text.toString() ) )
        binding.packetData2EditText.setText( hexModeGetText( binding.packetData2EditText.text.toString() ) )
        binding.packetData3EditText.setText( hexModeGetText( binding.packetData3EditText.text.toString() ) )
    }//.hexModeToggle()

    private fun fileOpen( uri: Uri) {
        val fileSize = contentResolver.openInputStream(uri)?.available()?.toInt() ?: 0
        if( fileSize > 0 ) {
            Log.d(TAG, "Open file: size=$fileSize, name=$uri")
            fileUri = uri
            binding.sendFileButton.isEnabled = true
            fileData = ByteArray(fileSize)
            contentResolver.openInputStream(uri)?.read( fileData )
            //statusTextViewAddText( sendFileData.decodeToString(0,5) )
        }
        // Alternative variant of read file
        // contentResolver.openInputStream(uri)?.bufferedReader(Charsets.US_ASCII)?.use { statusTextViewAddText( fileData.readText() ) }
    }//.openFileReadData()

    private fun statusTextViewAddText(text: String?, scrollTop:Boolean = false) {
        binding.consoleTextView.append(text)
        /// need fix scroll text
        if (scrollTop){ binding.consoleTextView.text = binding.consoleTextView.text }
        //val scrollAmount: Int = binding.consoleTextView.layout.getLineTop(binding.consoleTextView.lineCount) - binding.consoleTextView.height
        //if (scrollAmount > 0) binding.consoleTextView.scrollTo(0, scrollAmount)
    }//.statusTextViewAddText()



    companion object {
        // Used to load the 'serial_terminal' library on application startup.
        init {
            System.loadLibrary("serial_terminal")
        }
    }
}