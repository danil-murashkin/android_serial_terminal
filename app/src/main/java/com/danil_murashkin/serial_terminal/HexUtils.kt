package com.danil_murashkin.serial_terminal

class HexUtils {
    companion object {
        fun hexStringToByteArray(hexString: String): ByteArray? {
            var str: String = hexString
            str = str.replace(" ", "")
            str = str.replace("0x", "")
            str.forEach {if( !( ((it >= '0') && (it <= '9')) || ((it >= 'A') && (it <= 'F')) || ((it >= 'a') && (it <= 'f')) ) ) return null }
            str = str.uppercase()
            val len = str.length
            if (len % 2 > 0) { return null }

            val data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                val msb: Int? = str[i].digitToIntOrNull(16)
                val lsb: Int? = str[i+1].digitToIntOrNull(16)
                if( msb != null && lsb != null ) {
                    data[i / 2] = (msb * 16 + lsb).toByte()
                }
                i += 2
            }
            return data
        }

        fun bytesToHexString(byteArray: ByteArray): String? {
            var returnString = ""
            for (i in byteArray) {
                returnString += String.format("%02X", i)
            }
            return returnString
        }
    }
}