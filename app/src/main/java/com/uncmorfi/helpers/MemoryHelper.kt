package com.uncmorfi.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object MemoryHelper {

    fun saveToStorage(context: Context, file: String, content: String?) {
        if (content != null) {
            try {
                val out = OutputStreamWriter(
                        context.openFileOutput(file, Context.MODE_PRIVATE))

                out.write(content)
                out.close()
                Log.i("MemoryHelper", "Written $file success")
            } catch (ex: IOException) {
                Log.e("MemoryHelper", "Error writing in internal memory")
            }

        }
    }

    fun readStringFromStorage(context: Context, file: String): String? {
        try {
            val rd = BufferedReader(
                    InputStreamReader(context.openFileInput(file)))
            var line: String
            val read = StringBuilder()

            while (true) {
                line = rd.readLine() ?: break
                read.append(line)
            }

            rd.close()
            return read.toString()
        } catch (ex: Exception) {
            Log.e("MemoryHelper", "Error reading in internal memory")
            return null
        }

    }

    fun readHeadFromStorage(context: Context, file: String): String? {
        try {
            val rd = BufferedReader(
                    InputStreamReader(context.openFileInput(file)))

            val read = StringBuilder()
            var line: String

            // Leer las primeras 6 lineas
            // Lo suficiente para que aparezca alguna fecha
            for (i in 0..5) {
                line = rd.readLine()
                read.append(line)
            }
            rd.close()
            return read.toString()
        } catch (ex: Exception) {
            Log.e("MemoryHelper", "Error reading head in internal memory")
            return null
        }

    }

    fun saveBitmapToStorage(context: Context, file: String, bitmap: Bitmap?) {
        if (bitmap != null) {
            try {
                val out = context.openFileOutput(file, Context.MODE_PRIVATE)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.close()
                Log.i("MemoryHelper", "Written $file success")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("MemoryHelper", "Error writing barcode in internal memory")
            }

        }
    }

    fun readBitmapFromStorage(context: Context, file: String): Bitmap? {
        val bitmap: Bitmap
        try {
            bitmap = BitmapFactory.decodeStream(context.openFileInput(file))
        } catch (e: IOException) {
            Log.i("MemoryHelper", "Error reading barcode in internal memory")
            return null
        }

        return bitmap
    }

    fun deleteFileInStorage(context: Context, file: String) {
        val result = context.deleteFile(file)
        if (result)
            Log.i("MemoryHelper", "Deleted file $file")
        else
            Log.e("MemoryHelper", "Error deleting file $file")
    }
}