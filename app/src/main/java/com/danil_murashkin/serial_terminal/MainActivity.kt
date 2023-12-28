package com.danil_murashkin.serial_terminal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.util.Log
import com.danil_murashkin.serial_terminal.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example of a call to a native method
        binding.sampleText.text = stringFromJNI()


        val uart = UARTTTYMT2Operator();
        uart.open( "/dev/ttyMT2");
        val charset = Charsets.UTF_8
        val byteArrayWrite = "Ping\r\n".toByteArray(charset)
        Log.d("DEBUG", byteArrayWrite.toString(charset) )
        uart.write( byteArrayWrite, byteArrayWrite.size );

        val bytes = uart.read();
        if (bytes != null) {
            Log.d("DEBUG", bytes.toString(charset))
        }

        uart.close();
        Log.d("DEBUG", "End" )


    }

    /**
     * A native method that is implemented by the 'serial_terminal' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'serial_terminal' library on application startup.
        init {
            System.loadLibrary("serial_terminal")
        }
    }
}