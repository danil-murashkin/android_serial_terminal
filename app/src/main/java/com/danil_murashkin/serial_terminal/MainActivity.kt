package com.danil_murashkin.serial_terminal

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.danil_murashkin.serial_terminal.databinding.ActivityMainBinding
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.charset.Charset


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

        //openFileIntentActivity.launch( "audio/wav" )
        openFileActivity.launch( "text/plain" )
    }

    private val openFileActivity = registerForActivityResult( ActivityResultContracts.GetContent() ) // OpenDocument
    { uri ->
        if (uri != null) {
            binding.sampleText.text = uri.path
            val fileRead:File = File(uri.path)

            //val stream: FileInputStream = openFileInput(uri.path)
            //val data = ByteArray(1000)
            //stream.read(data)
            //stream.close()
        }
    }

    companion object {
        // Used to load the 'serial_terminal' library on application startup.
        init {
            System.loadLibrary("serial_terminal")
        }
    }
}