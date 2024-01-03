@file:Suppress("DEPRECATION")

package com.danil_murashkin.serial_terminal

import android.R
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock.sleep
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.danil_murashkin.serial_terminal.databinding.ActivityMainBinding
import java.io.File
import java.io.FileReader
import java.io.LineNumberReader


class MainActivity : AppCompatActivity() {
    private val TAG:String = "SerialTerminal"
    private lateinit var binding: ActivityMainBinding

    private val uartDefaultPortName = "/dev/tty1WK2"
    private val uart = UARTPort();


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

        binding.packetData1EditText.setText("BUS+AUDIO=")
        binding.packetData2EditText.setText("BUS+STATION=30A")
        binding.packetData3EditText.setText("BUS+STATION=30B")
        binding.fileChunkSizeEditText.setText("4410")
        binding.uartConnectButton.setOnClickListener{ uartConnectButtonClick() }
        binding.sendPacket1Button.setOnClickListener{ sendPacket1ButtonClick() }
        binding.sendPacket2Button.setOnClickListener{ sendPacket2ButtonClick() }
        binding.sendPacket3Button.setOnClickListener{ sendPacket3ButtonClick() }
        binding.openFileButton.setOnClickListener{ openFileButtonClick() }
        binding.sendFileButton.setOnClickListener { sendFileButtonClick() }

        val uartPorts: MutableList<String> = ArrayList()
        uartPorts.addAll( uart.getAvailablePorts() )
        val dataAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, uartPorts)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.uartPortsSpinner.adapter = dataAdapter
        uartPorts.forEachIndexed { index, portName ->
            if (portName == uartDefaultPortName) {
                Log.d(TAG, "Default port $uartDefaultPortName founded")
                binding.uartPortsSpinner.setSelection(index)
            }
        }

        var  task:MyAsyncTask = MyAsyncTask()
        task.execute()
        Log.d(TAG, "Second")
    }

    inner class MyAsyncTask: AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String? {
            try {
                while (true) {
                    sleep(1000);

                    val bytes = uart.read(512);
                    if (bytes != null) {
                        val charset = Charsets.US_ASCII
                        Log.d(TAG, bytes.toString(charset))
                        binding.consoleTextView.text = bytes.toString(charset)
                    }

                }
            } finally {
            }
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result != null) {
                Log.d(TAG, result)
            }
        }
        fun tets(str: String) {
            Log.d(TAG, str)
        }
    }



    private var uartConnectedFlag:Boolean = false
    private fun uartConnectButtonClick() {

        val uartPortName:String = binding.uartPortsSpinner.adapter.getItem( binding.uartPortsSpinner.selectedItemPosition ).toString()
        if(!uartConnectedFlag) {
            val TRUE:Int = 1
            if( uart.open( uartPortName, 460800 ) == TRUE )
            {
                binding.statusTextView.text = "Connected to: $uartPortName"
                uartConnectedFlag = true
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


    private fun uartSendText(text:String) {
        val charset = Charsets.US_ASCII
        val byteArrayWrite = binding.packetData1EditText.text.toString().toByteArray(charset)
        Log.d(TAG, byteArrayWrite.toString(charset) )
        uart.write( byteArrayWrite, byteArrayWrite.size );
    }
    private fun sendPacket1ButtonClick() { uartSendText( binding.packetData1EditText.text.toString() ) }
    private fun sendPacket2ButtonClick() { uartSendText( binding.packetData2EditText.text.toString() ) }
    private fun sendPacket3ButtonClick() { uartSendText( binding.packetData3EditText.text.toString() ) }
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





    companion object {
        // Used to load the 'serial_terminal' library on application startup.
        init {
            System.loadLibrary("serial_terminal")
        }
    }
}