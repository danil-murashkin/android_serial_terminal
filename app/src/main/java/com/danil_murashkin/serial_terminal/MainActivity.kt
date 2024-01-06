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
import java.io.BufferedReader
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

    lateinit var sendFileData:ByteArray
    private var sendFileUri:Uri = Uri.parse( "content://com.android.providers.downloads.documents/document/msf%3A55" )
    //private val sendFileUri:Uri = Uri.parse( "content://com.android.providers.downloads.documents/document/msf%3A56" ) // Test.txt
    private var hexModeFlag:Boolean = false
    private var fileChunkSize:Int = 4410
    private val readFileActivity = registerForActivityResult( ActivityResultContracts.OpenDocument() ) { uri ->  if (uri != null) { sendFileRead(uri) } }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example of a call to a native method
        binding.statusTextView.text = "Ready for connection"
        binding.consoleTextView.text = ""
        binding.uartPortBaudrateEditText.setText( uartPortBaudrate.toString() )
        binding.fileChunkSizeEditText.setText( fileChunkSize.toString() )
        binding.sendFileButton.isEnabled = false
        binding.packetData1EditText.setText("BUS+AUDIO=")
        binding.packetData2EditText.setText("BUS+STATION=30A")
        binding.packetData3EditText.setText("BUS+STATION=30B")

        binding.hexModeButton.setOnClickListener{ hexModeToggle() }
        binding.clearButton.setOnClickListener{ binding.consoleTextView.text = "" }
        binding.uartConnectButton.setOnClickListener{ uartConnect( binding.uartPortsSpinner.adapter.getItem( binding.uartPortsSpinner.selectedItemPosition ).toString(), binding.uartPortBaudrateEditText.text.toString().toInt() ) }
        binding.sendPacket1Button.setOnClickListener{ uartSendText( binding.packetData1EditText.text.toString() ) }
        binding.sendPacket2Button.setOnClickListener{ uartSendText( binding.packetData2EditText.text.toString() ) }
        binding.sendPacket3Button.setOnClickListener{ uartSendText( binding.packetData3EditText.text.toString() ) }
        binding.openFileButton.setOnClickListener{ readFileActivity.launch( arrayOf("*/*") ) } // "audio/*", "text/plain"
        binding.sendFileButton.setOnClickListener { /* ///! */ }

        uartUpdatePortsAtSpinner()
        //uartConnect( binding.uartPortsSpinner.adapter.getItem( binding.uartPortsSpinner.selectedItemPosition ).toString(), wbinding.uartPortBaudrateEditText.text.toString().toInt() )
        sendFileRead( sendFileUri )
    }//.onCreate()



    private fun hexModeToggle() {
        if( !hexModeFlag )
        {
            //HexUtils.hexStringToByteArray( "asdasd")
            hexModeFlag = true
            binding.hexModeButton.text = "ASCII"

        } else {
            hexModeFlag = false
            binding.hexModeButton.text = "HEX"
        }
    }//.hexModeToggle()

    private fun uartSendText(text:String) {
        if( uartConnectedFlag ) {
            val charset = Charsets.US_ASCII
            val byteArrayWrite = text.toByteArray(charset)
            uart.write(byteArrayWrite, byteArrayWrite.size)
            Log.d(TAG, "Uart send: $text")
        }
    }

    inner class mainWhileReadUartAsyncTask: AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String? {
            try {
                TimeUnit.MICROSECONDS.sleep(100)
                while (true) {
                    TimeUnit.MICROSECONDS.sleep(10)
                    if( uartConnectedFlag ) {
                        val bytes = uart.read()
                        if (bytes != null) {
                            val charset = Charsets.US_ASCII
                            Log.d(TAG, bytes.toString(charset))
                            statusTextViewAddText( bytes.toString(charset) )
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

    private fun sendFileRead( uri: Uri) {
        val fileSize = contentResolver.openInputStream(uri)?.available()?.toInt() ?: 0
        if( fileSize > 0 ) {
            Log.d(TAG, "Open file: size-$fileSize, name-$uri")
            sendFileUri = uri
            binding.sendFileButton.isEnabled = true
            sendFileData = ByteArray(fileSize)
            contentResolver.openInputStream(uri)?.read( sendFileData )
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