package com.danil_murashkin.serial_terminal

class HexUtils {
    companion object {
        fun hexStringToByteArray(hexString: String): ByteArray? {
            var str: String = hexString
            str = str.replace(" ", "")
            str = str.replace("0x", "")
            val len = str.length


            if (len % 2 > 0) {
                return null
            }
            val data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                data[i / 2] =
                    ((str[i].digitToIntOrNull(16) ?: -1 shl 4) + str[i + 1].digitToIntOrNull(16)!!
                        ?: -1).toByte()
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