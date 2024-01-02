@file:Suppress("DEPRECATION")

package com.danil_murashkin.serial_terminal

import android.R
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.danil_murashkin.serial_terminal.databinding.ActivityMainBinding
import java.io.File
import java.io.FileReader
import java.io.LineNumberReader


class MainActivity : AppCompatActivity() {

    private val TAG = "SerialTerminal"
    private val uartDefaultPortName = "/dev/tty1WK2"
    private lateinit var binding: ActivityMainBinding

    var runningTask: AsyncTask<*, *, *>? = null
    


    ///?  Saving EditText and retrieve it automatically
    /// https://www.digitalocean.com/community/tutorials/android-shared-preferences-example-tutorial
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example of a call to a native method
        binding.statusTextView.text = "Ready for connection"
        binding.consoleTextView.text = ""
        binding.sendFileButton.isEnabled = false
        "/dev/tty1WK2"

        binding.packetData1EditText.setText("BUS+AUDIO=")
        binding.packetData2EditText.setText("BUS+STATION=30A")
        binding.packetData3EditText.setText("BUS+STATION=30B")
        binding.fileChunkSizeEditText.setText("4410")
        binding.uartConnectButton.setOnClickListener{ uartConnectButtonClick() }
        binding.sendPacket3Button.setOnClickListener{ sendPacket3ButtonClick() }
        binding.sendPacket3Button.setOnClickListener{ sendPacket2ButtonClick() }
        binding.sendPacket3Button.setOnClickListener{ sendPacket1ButtonClick() }
        binding.openFileButton.setOnClickListener{ openFileButtonClick() }
        binding.sendFileButton.setOnClickListener { sendFileButtonClick() }

        getDevices()
    }

    @Suppress("DEPRECATION")
    private class LongOperation : AsyncTask<Void?, Void?, String>() {
        protected override fun doInBackground(vararg params: Void?): String? {
            for (i in 0..4) {
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    // We were cancelled; stop sleeping!
                }
            }

            return "Executed"
        }

        override fun onPostExecute(result: String) {
            val txt = findViewById<View>(R.id.consoleTextView) as TextView
            txt.text = "Executed" // txt.setText(result);
            // You might want to change "executed" for the returned string
            // passed into onPostExecute(), but that is up to you
        }
    }



    private var uartConnectedFlag:Boolean = false
    private fun uartConnectButtonClick() {
        val uart = UARTPort();
        val uartPortName:String = binding.uartPortsSpinner.adapter.getItem( binding.uartPortsSpinner.selectedItemPosition ).toString()
        if(!uartConnectedFlag) {
            val TRUE:Int = 1
            if( uart.open( uartPortName ) == TRUE )
            {
                binding.statusTextView.text = "Connected to: $uartPortName"
                uartConnectedFlag = true
                binding.uartConnectButton.text = "DISCONNECT"

                val charset = Charsets.US_ASCII
                val byteArrayWrite = "Ping\r\n".toByteArray(charset)
                Log.d(TAG, byteArrayWrite.toString(charset) )
                //uart.write( byteArrayWrite, byteArrayWrite.size );
                val bytes = uart.read(512);
                if (bytes != null) {
                    Log.d(TAG, bytes.toString(charset))
                    binding.consoleTextView.text = bytes.toString(charset)
                }
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



    private fun sendPacket1ButtonClick() {
        //packetData1EditText
        runningTask = LongOperation()
        (runningTask as LongOperation).execute()
    }
    private fun sendPacket2ButtonClick() {
        //packetData2EditText
    }
    private fun sendPacket3ButtonClick() {
        //packetData3EditText
    }
    private fun sendFileButtonClick() {
        //fileChunkSizeEditText
    }
    // https://gist.github.com/s3va/85c1c330f9786c60903934d7e3e8f479
    private fun openFileButtonClick() {
        binding.statusTextView.text = "Button click"
        openFileActivity.launch( arrayOf("*/*") ) // "text/plain" "audio/wav"
    }

    private val openFileActivity = registerForActivityResult( ActivityResultContracts.OpenDocument() )
    { uri ->
        if (uri != null) {
            contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                binding.consoleTextView.text = it.readText()
                val scrollAmount: Int = binding.consoleTextView.layout.getLineTop(binding.consoleTextView.lineCount) - binding.consoleTextView.height
                if (scrollAmount > 0) binding.consoleTextView.scrollTo(0, scrollAmount)

                binding.sendFileButton.isEnabled = true
            }
        }
    }



    private fun getDevices() {
        val devDrivers: MutableList<String> = ArrayList()
        val uartPorts: MutableList<String> = ArrayList()

        try {
            val lineNumberReader = LineNumberReader(FileReader( "/proc/tty/drivers" ))
            while (true) {
                val line = lineNumberReader.readLine()
                if (line != null) {
                    val lineString: String = line.toString()
                    val foundSerialIndex = lineString.indexOf("serial", startIndex = 10)
                    if (foundSerialIndex > 10) {
                        val foundDevPathBegin = lineString.indexOf("/dev/", startIndex = 10)
                        val foundDevPathEnd = lineString.indexOf(" ", startIndex = foundDevPathBegin)
                        val devDriverPath: String = lineString.substring(foundDevPathBegin, foundDevPathEnd).trim()
                        devDrivers.add(devDriverPath)
                    }
                } else {
                    break
                }
            }
            if (devDrivers.size > 0) {
                Log.d(TAG, "Found serial drivers: $devDrivers")
            }
        }catch( e : Exception)  {
            Log.d(TAG, "Serial drivers not found")
            devDrivers.add("/dev/tty")
        }

        try {
            val dev = File("/dev")
            val files = dev.listFiles()
            var i: Int = 0
            while (i < files.size) {
                devDrivers.forEach {
                    if (files[i].absolutePath.startsWith(it)) {
                        uartPorts.add(files[i].toString())
                    }
                }
                i++
            }
            if (devDrivers.size > 0) {
                Log.d(TAG, "Found serial ports: $uartPorts")
            }
        }catch( e : Exception)  {
            Log.d(TAG, "Serial ports not found")
            uartPorts.add("Serial not found")
        }

        val dataAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, uartPorts)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.uartPortsSpinner.adapter = dataAdapter
        uartPorts.forEachIndexed { index, portName ->
            if (portName == uartDefaultPortName) {
                Log.d(TAG, "Default port $uartDefaultPortName founded")
                binding.uartPortsSpinner.setSelection(index)
            }
        }
    }//.getDevices()



    companion object {
        // Used to load the 'serial_terminal' library on application startup.
        init {
            System.loadLibrary("serial_terminal")
        }
    }
}