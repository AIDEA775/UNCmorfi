package com.uncmorfi.menu

import android.os.AsyncTask
import com.uncmorfi.helpers.ConnectionHelper
import java.io.IOException

/**
 * Descarga el menú de la semana.
 */
internal class RefreshMenuTask(private val mListener: (String?) -> Unit) :
        AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg params: Void): String? {
        return try {
            ConnectionHelper.downloadFromUrlByGet(URL)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun onPostExecute(result: String?) {
        mListener(result)
    }

    companion object {
        private const val URL = "http://uncmorfi.georgealegre.com/menu"
    }
}