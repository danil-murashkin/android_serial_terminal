package com.danil_murashkin.serial_terminal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.danil_murashkin.serial_terminal.databinding.ActivityMainBinding
import java.io.File


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
    fun openFileButtonClick() {
        binding.sampleText.text = "Olololo"

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        val uri = Uri.parse(
            Environment.getExternalStorageDirectory().path
                    + File.separator + "myFolder" + File.separator
        )
        intent.setDataAndType(uri, "audio/wav")
        startActivity(Intent.createChooser(intent, "Open folder"))
    }

    companion object {
        // Used to load the 'serial_terminal' library on application startup.
        init {
            System.loadLibrary("serial_terminal")
        }
    }
}