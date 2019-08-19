package com.uncmorfi.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.IOException

fun Context.saveToStorage(file: String, bitmap: Bitmap?) {
    if (bitmap != null) {
        try {
            val out = this.openFileOutput(file, Context.MODE_PRIVATE)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.close()
            Log.i("MemoryHelper", "Written $file success")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("MemoryHelper", "Error writing barcode in internal memory")
        }

    }
}

fun Context.readBitmapFromStorage(file: String): Bitmap? {
    val bitmap: Bitmap
    try {
        bitmap = BitmapFactory.decodeStream(this.openFileInput(file))
    } catch (e: IOException) {
        Log.i("MemoryHelper", "Error reading barcode in internal memory")
        return null
    }

    return bitmap
}

fun Context.deleteFileInStorage(file: String) {
    val result = this.deleteFile(file)
    if (result)
        Log.i("MemoryHelper", "Deleted file $file")
    else
        Log.e("MemoryHelper", "Error deleting file $file")
}
