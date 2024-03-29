// UARTOperator.kt
// Based on BevisWang from 2018/7/17
// URL https://blog.csdn.net/PD_Wang/article/details/81449768

package com.danil_murashkin.serial_terminal

import java.io.Closeable



interface UARTOperator : Closeable {
    fun open( path: String, baudRate: Int = 115200, dataBits: Int = 8, stopBits: Int = 1, parity: Char = 'N' ):Int
    fun write(data: ByteArray, data_len: Int)
    fun read(maxSize: Int = 256, timeout:Int = 0): ByteArray?
}