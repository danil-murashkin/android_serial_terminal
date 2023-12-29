package com.danil_murashkin.serial_terminal

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.danil_murashkin.serial_terminal.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import javax.xml.transform.stream.StreamResult




class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example of a call to a native method
        binding.sampleText.text = "Nope"
        var openFileButton = findViewById<View>(R.id.openFileButton) as Button
        openFileButton.setOnClickListener(View.OnClickListener { openFileButtonClick() })



        /*val uart = UARTTTYMT2Operator();
        uart.open( "/dev/tty1WK2");
        val charset = Charsets.UTF_8
        val byteArrayWrite = "Ping\r\n".toByteArray(charset)
        Log.d("DEBUG", byteArrayWrite.toString(charset) )
        uart.write( byteArrayWrite, byteArrayWrite.size );

        val bytes = uart.read();
        if (bytes != null) {
            Log.d("DEBUG", bytes.toString(charset))
            binding.sampleText.text = bytes.toString(charset)
        }
        uart.close();*/

    }



    private fun openFileButtonClick()
    {
        binding.sampleText.text = "Button click"

        openFileActivity.launch( arrayOf("*/*") ) // "text/plain" "audio/wav"
    }
    private val openFileActivity = registerForActivityResult( ActivityResultContracts.OpenDocument() )
    { uri ->
        if (uri != null) {
            contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                binding.textView.setText( it.readText() )
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