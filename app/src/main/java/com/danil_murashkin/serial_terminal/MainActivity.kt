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
    private val TAG:String? = "SerialTerminal"
    private val uartDefaultPortName = "/dev/tty1WK2"
    private lateinit var binding: ActivityMainBinding
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
            sleep(10000);
            return "First"
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result != null) {
                Log.d(TAG, result)
            }
        }
    }



    private var uartConnectedFlag:Boolean = false
    private fun uartConnectButtonClick() {

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





    companion object {
        // Used to load the 'serial_terminal' library on application startup.
        init {
            System.loadLibrary("serial_terminal")
        }
    }
}