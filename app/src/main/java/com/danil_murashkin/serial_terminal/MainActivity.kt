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

        //openFileIntentActivity.launch( "audio/wav" )
        //openFileActivity.launch( "text/plain" )
        //openFileActivity.launch( arrayOf("text/plain") )
        resultContracts.launch( arrayOf("text/plain") )
    }
    private var uri: Uri? = null
    private val TAG = "MainActivity"
    private val resultContracts =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
            if (u == null) {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            uri = u
            contentResolver.openInputStream(u)?.bufferedReader()?.use {
                binding.textView.setText(it.readText())
            }

            contentResolver.query(u, null, null, null, null)?.let { cursor ->
                cursor.moveToFirst()

                val fn = cursor.getStringOrNull(
                    cursor.getColumnIndex(
                        android.provider.OpenableColumns.DISPLAY_NAME
                    )
                )
                val did = DocumentsContract.getDocumentId(u)

                val doc_id =
                    cursor.getStringOrNull(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID))
                val mime_type =
                    cursor.getStringOrNull(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE))
                val l_modified =
                    cursor.getLongOrNull(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED))
                val sz =
                    cursor.getLongOrNull(cursor.getColumnIndex(android.provider.OpenableColumns.SIZE))
                val fl =
                    cursor.getIntOrNull(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_FLAGS))

                //if (fl != null) writeBtn((fl and DocumentsContract.Document.FLAG_SUPPORTS_WRITE) != 0)


                Log.i(TAG, "---- column: ${cursor.columnNames}")

                Log.i(TAG, "getDocumentId: $did")
                Log.i(
                    TAG,
                    "uri: $u\ndocument_id: $doc_id\nmime_tipe: $mime_type\nlast modified: $l_modified\nsize: $sz\nflags: $fl"
                )

                for (i: Int in 0 until cursor.columnCount) {
                    Log.i(TAG, cursor.getColumnName(i) + " : " + cursor.getType(i) + " : ")
                }
                cursor.close()

                when (val p = checkUriPermission(
                    uri,
                    Binder.getCallingPid(),
                    Binder.getCallingUid(),
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )) {
                    PackageManager.PERMISSION_GRANTED -> {
                        Toast.makeText(
                            this,
                            "Write Permission Granted to: $fn\n(size: $sz)\n",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    PackageManager.PERMISSION_DENIED ->
                        Toast.makeText(
                            this,
                            "Write Permission Denied!!!! filename: $fn\n(size: $sz)",
                            Toast.LENGTH_LONG
                        ).show()
                    else ->
                        Toast.makeText(
                            this,
                            "Down Know what is this permission: $p\nfilename: $fn (size: $sz)",
                            Toast.LENGTH_LONG
                        ).show()
                }

                title = "$fn opened"

                val cursor2 = contentResolver.query(
                    u,
                    arrayOf(DocumentsContract.Document.COLUMN_FLAGS),
                    null,
                    null,
                    null
                )
                val flags = if (cursor2?.moveToFirst() == true && !cursor2.isNull(0)) {
                    cursor2.getInt(0)
                } else {
                    0
                }

                Log.i(
                    TAG,
                    "-------- flags: $flags --- fl: $fl -------- isWritable: ${(flags and DocumentsContract.Document.FLAG_SUPPORTS_WRITE) != 0} --------------"
                )
            }
        }

    private val openFileActivity = registerForActivityResult( ActivityResultContracts.OpenDocument() ) // GetContent
    { uri ->
        if (uri != null) {
            binding.sampleText.text = uri.path
            val fileRead:File = File(uri.path)
            val stream: FileInputStream = openFileInput(uri.path)
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