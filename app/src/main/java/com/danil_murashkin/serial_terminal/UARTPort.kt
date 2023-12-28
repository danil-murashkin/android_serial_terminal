// UARTTTYMT2Operator.kt
//
// Created by BevisWang on 2018/7/17.
//
// URL https://blog.csdn.net/PD_Wang/article/details/81449768

package com.danil_murashkin.serial_terminal

import android.util.Log



class UARTTTYMT2Operator : IUARTOperator {

    override fun open(path: String, baudRate: Int, dataBits: Int, stopBits: Int, parity: Char) {
        //val result = openPort(path, baudRate, dataBits, stopBits, parity)
        //if (result != 1) {
            Log.e("DEBUG","Serial port open failed！")
        //}
    }

    override fun write(data: ByteArray, data_len: Int) {
        //writePort(data, data_len)
    }

    override fun read(maxSize: Int): ByteArray? {
        // return readPort(maxSize)
        return null;
    }

    override fun close() {
        //closePort()
    }


    private external fun openPort(path: String = PORT_PATH, baudRate: Int = 115200, dataBits: Int = 8, stopBits: Int = 1, parity: Char = 'N'): Int
    private external fun readPort(maxSize: Int): ByteArray?
    private external fun writePort(data: ByteArray, data_len: Int)
    private external fun closePort()

    companion object {
        private const val PORT_PATH = "/dev/ttyMT2"

        init {
            System.loadLibrary("serial_terminal")
        }
    }
}