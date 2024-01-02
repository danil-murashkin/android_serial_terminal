package com.danil_murashkin.serial_terminal

import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.danil_murashkin.serial_terminal.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    ///?  Saving EditText and retrieve it automatically
    /// https://www.digitalocean.com/community/tutorials/android-shared-preferences-example-tutorial
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example of a call to a native method
        binding.sampleText.text = "Nope"
        binding.textView.text = "Nope"
        binding.uartConnectButton.setOnClickListener{ uartConnectButtonClick() }
        binding.sendPacket3Button.setOnClickListener{ sendPacket3ButtonClick() }
        binding.sendPacket3Button.setOnClickListener{ sendPacket2ButtonClick() }
        binding.sendPacket3Button.setOnClickListener{ sendPacket1ButtonClick() }
        binding.openFileButton.setOnClickListener{ openFileButtonClick() }
        binding.sendFileButton.setOnClickListener { sendFileButtonClick() }
        binding.sendFileButton.isEnabled = false
        binding.sampleText.text =  "Ready for connection"
    }




    private var uartConnectedFlag:Boolean = false
    private fun uartConnectButtonClick()
    {
        /*
        val charset = Charsets.UTF_8
        val byteArrayWrite = "Ping\r\n".toByteArray(charset)
        Log.d("DEBUG", byteArrayWrite.toString(charset) )
        uart.write( byteArrayWrite, byteArrayWrite.size );

        val bytes = uart.read();
        if (bytes != null) {
            Log.d("DEBUG", bytes.toString(charset))
            binding.sampleText.text = bytes.toString(charset)
        }
        */

        val uart = UARTPort();
        if(!uartConnectedFlag)
        {
            val TRUE:Int = 1
            if( uart.open( binding.uartPortNameEditText.text.toString() ) == TRUE )
            {
                binding.sampleText.text =  "Connected to port: " + binding.uartPortNameEditText.text
                uartConnectedFlag = true
                binding.uartConnectButton.text = "DISCONNECT"

                val charset = Charsets.UTF_8
                val byteArrayWrite = "Ping\r\n".toByteArray(charset)
                Log.d("DEBUG", byteArrayWrite.toString(charset) )
                uart.write( byteArrayWrite, byteArrayWrite.size );

                val bytes = uart.read();
                if (bytes != null) {
                    Log.d("DEBUG", bytes.toString(charset))
                    binding.sampleText.text = bytes.toString(charset)
                }
                uart.close();
            }
            else
            {
                uart.close();
                binding.sampleText.text =  "Failed connection to port: " + binding.uartPortNameEditText.text
            }
        }
        else
        {
            binding.sampleText.text =  "Ready for connection"
            uartConnectedFlag = false
            binding.uartConnectButton.text = "CONNECT"

            uart.close();
        }
    }
    //.uartConnectButtonClick()
    private fun sendPacket3ButtonClick()
    {
        //packetData3EditText
    }
    private fun sendPacket2ButtonClick()
    {
        //packetData2EditText
    }
    private fun sendPacket1ButtonClick()
    {
        //packetData1EditText
    }
    private fun sendFileButtonClick()
    {
        //fileChunkSizeEditText
    }
    // https://gist.github.com/s3va/85c1c330f9786c60903934d7e3e8f479
    private fun openFileButtonClick()
    {
        binding.sampleText.text = "Button click"
        openFileActivity.launch( arrayOf("*/*") ) // "text/plain" "audio/wav"
    }

    private val openFileActivity = registerForActivityResult( ActivityResultContracts.OpenDocument() )
    { uri ->
        if (uri != null) {
            contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                binding.textView.text = it.readText()
                val scrollAmount: Int = binding.textView.layout.getLineTop(binding.textView.lineCount) - binding.textView.height
                if (scrollAmount > 0) binding.textView.scrollTo(0, scrollAmount)

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