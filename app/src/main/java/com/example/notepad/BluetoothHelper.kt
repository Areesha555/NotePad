package com.example.notepad

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.experimental.or

class BluetoothHelper {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    private fun hasBluetoothConnectPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isBluetoothEnabled(context: Context): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }

    private fun getFirstPairedDevice(context: Context): BluetoothDevice? {
        if (!hasBluetoothConnectPermission(context)) {
            Log.e("Bluetooth", "BLUETOOTH_CONNECT permission not granted")
            return null
        }

        return try {
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            pairedDevices?.firstOrNull()
        } catch (e: SecurityException) {
            Log.e("Bluetooth", "Error accessing bonded devices: ${e.message}")
            null
        }
    }

    private fun connectToDevice(device: BluetoothDevice, context: Context): Boolean {
        if (!hasBluetoothConnectPermission(context)) {
            Log.e("Bluetooth", "BLUETOOTH_CONNECT permission not granted")
            return false
        }

        return try {
            val uuid = try {
                device.uuids?.firstOrNull()?.uuid ?: run {
                    device.fetchUuidsWithSdp()
                    device.uuids?.firstOrNull()?.uuid ?: return false
                }
            } catch (e: SecurityException) {
                Log.e("Bluetooth", "Error fetching UUIDs: ${e.message}")
                return false
            }

            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothAdapter?.cancelDiscovery()
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            true
        } catch (e: SecurityException) {
            Log.e("Bluetooth", "SecurityException while connecting: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e("Bluetooth", "Connection failed: ${e.message}")
            false
        }
    }



    suspend fun printBitmap(context: Context, bitmap: Bitmap, textBelow: String) {
        withContext(Dispatchers.IO) {
            try {
                if (!hasBluetoothConnectPermission(context)) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Bluetooth permission not granted", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }

                if (!isBluetoothEnabled(context)) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }

                val printerDevice = getFirstPairedDevice(context)
                if (printerDevice == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No paired printer found. Please pair a Bluetooth printer first.", Toast.LENGTH_LONG).show()
                    }
                    return@withContext
                }

                if (!connectToDevice(printerDevice, context)) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Could not connect to printer", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }

                // Resize bitmap to printer width (e.g., 384px for 58mm printers)
                val resizedBitmap = resizeBitmapToPrinterWidth(bitmap, 576)

                // Convert and print
                val imageData = convertBitmapToEscPos(resizedBitmap)
                outputStream?.write(imageData)

                // Add text below
                outputStream?.write("\n$textBelow\n\n".toByteArray())

                // Optional cut command
                outputStream?.write(byteArrayOf(0x1D, 0x56, 0x41, 0x10))

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Printed successfully", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("Bluetooth", "Printing failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Printing failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun resizeBitmapToPrinterWidth(bitmap: Bitmap, targetWidth: Int): Bitmap {
        val aspectRatio = bitmap.height.toDouble() / bitmap.width.toDouble()
        val targetHeight = (targetWidth * aspectRatio).toInt()
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    fun convertBitmapToEscPos(bitmap: Bitmap): ByteArray {

        val bmp = bitmap.copy(Bitmap.Config.ARGB_8888, false)
        val width = bmp.width
        val height = bmp.height

        val bytesPerRow = (width + 7) / 8
        val imageData = ByteArray(height * bytesPerRow)

        var index: Int
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bmp.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                // Calculate grayscale value (0-255)
                val gray = (r + g + b) / 3

                // If darker than threshold, make it black (1); otherwise white (0)
                if (gray < 128) {
                    index = y * bytesPerRow + x / 8
                    imageData[index] = imageData[index] or (0x80 ushr (x % 8)).toByte()
                }
            }
        }

        // ESC/POS Raster Bit Image Command: GS v 0
        val escPosHeader = byteArrayOf(
            0x1D, 0x76, 0x30, 0x00,                   // GS v 0 m (m=0: normal)
            (bytesPerRow % 256).toByte(),             // xL: width in bytes (low byte)
            (bytesPerRow / 256).toByte(),             // xH: width in bytes (high byte)
            (height % 256).toByte(),                  // yL: height in dots (low byte)
            (height / 256).toByte()                   // yH: height in dots (high byte)
        )

        return escPosHeader + imageData
    }




    fun closeConnection() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: Exception) {
            Log.e("Bluetooth", "Close connection failed: ${e.message}")
        }
    }
}
