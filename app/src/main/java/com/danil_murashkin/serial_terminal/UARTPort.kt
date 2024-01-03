// UARTTTYMT2Operator.kt
// Based on BevisWang from 2018/7/17
// URL https://blog.csdn.net/PD_Wang/article/details/81449768

package com.danil_murashkin.serial_terminal

import android.R
import android.util.Log
import android.widget.ArrayAdapter
import java.io.File
import java.io.FileReader
import java.io.LineNumberReader


class UARTPort : UARTOperator {
    private val TAG : String? = "UARTPort"

    private val TRUE:Int = 1
    override fun open(path: String, baudRate: Int, dataBits: Int, stopBits: Int, parity: Char):Int {
        val result = openPort(path, baudRate, dataBits, stopBits, parity)
        if (result != TRUE) {
            Log.e("DEBUG","Serial port open failed！")
        }
        return result
    }

    override fun write(data: ByteArray, data_len: Int) {
        writePort(data, data_len)
    }

    override fun read(maxSize: Int): ByteArray? {
        return readPort(maxSize)
    }

    override fun close() {
        closePort()
    }



    private external fun openPort(path: String, baudRate: Int = 115200, dataBits: Int = 8, stopBits: Int = 1, parity: Char = 'N'): Int
    private external fun readPort(maxSize: Int): ByteArray?
    private external fun writePort(data: ByteArray, data_len: Int)
    private external fun closePort()

    companion object {
        init {
            System.loadLibrary("serial_terminal")
        }
    }



    fun getAvailablePorts(): MutableList<String> {
        // About serial drivers https://www.oreilly.com/library/view/linux-device-drivers/0596005903/ch18.html

        val devDrivers: MutableList<String> = ArrayList()
        val uartPorts: MutableList<String> = ArrayList()

        try {
            val lineNumberReader = LineNumberReader(FileReader( "/proc/tty/drivers" ))
            while (true) {
                val line = lineNumberReader.readLine()
                if (line != null) {
                    val lineString: String = line.toString()
                    // Log.d(TAG, "Found driver: $lineString")
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

        return  uartPorts
    }//.getAvailablePorts()

}