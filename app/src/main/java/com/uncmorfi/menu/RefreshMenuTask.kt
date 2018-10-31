package com.uncmorfi.menu

import android.os.AsyncTask
import com.uncmorfi.helpers.downloadByGet
import java.io.IOException
import java.net.URL

/**
 * Descarga el menú de la semana.
 */
internal class RefreshMenuTask(private val mListener: (String?) -> Unit) :
        AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg params: Void): String? {
        return try {
            URL(url).downloadByGet()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun onPostExecute(result: String?) {
        mListener(result)
    }

    companion object {
        private const val url = "http://uncmorfi.georgealegre.com/menu"
    }
}