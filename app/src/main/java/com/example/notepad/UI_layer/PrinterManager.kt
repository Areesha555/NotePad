package com.example.notepad.UI_layer

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.experimental.or

class PrinterManager {

    suspend fun isPrinterReachable(ip: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(ip, 9100), 3000)
                socket.close()
                true
            } catch (e: IOException) {
                false
            }
        }
    }

    suspend fun printBitmap(ip: String, bitmap: Bitmap): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val escPosData = bitmapToEscPosCommands(bitmap)
                val socket = Socket()
                socket.connect(InetSocketAddress(ip, 9100), 3000)
                socket.getOutputStream().use { outputStream ->
                    outputStream.write(escPosData)
                    outputStream.flush()
                }
                socket.close()
                true
            } catch (e: IOException) {
                false
            }
        }
    }




    fun bitmapToEscPosCommands(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height


        val widthBytes = (width + 7) / 8
        val imageBytes = ByteArray(widthBytes * height)


        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                val luminance = (0.299 * ((pixel shr 16) and 0xFF) +
                        0.587 * ((pixel shr 8) and 0xFF) +
                        0.114 * (pixel and 0xFF))

                if (luminance < 128) {
                    val byteIndex = y * widthBytes + x / 8
                    imageBytes[byteIndex] = imageBytes[byteIndex] or (0x80 shr (x % 8)).toByte()
                }
            }
        }


        val xL = (widthBytes and 0xFF).toByte()
        val xH = ((widthBytes shr 8) and 0xFF).toByte()
        val yL = (height and 0xFF).toByte()
        val yH = ((height shr 8) and 0xFF).toByte()

        val command = ByteArray(8 + imageBytes.size)
        command[0] = 0x1D // GS
        command[1] = 0x76 // 'v'
        command[2] = 0x30 // '0'
        command[3] = 0x00 // mode: normal
        command[4] = xL
        command[5] = xH
        command[6] = yL
        command[7] = yH


        System.arraycopy(imageBytes, 0, command, 8, imageBytes.size)

        return command
    }

}
