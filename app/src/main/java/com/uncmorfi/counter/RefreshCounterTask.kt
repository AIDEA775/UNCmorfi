package com.uncmorfi.counter

import android.os.AsyncTask
import com.github.mikephil.charting.data.Entry
import com.uncmorfi.helpers.ConnectionHelper
import com.uncmorfi.helpers.ParserHelper
import com.uncmorfi.helpers.clearDate
import com.uncmorfi.helpers.toDate
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * Descarga y parsea los datos obtenidos del medidor de raciones.
 */
internal class RefreshCounterTask(private val mListener: (resultCode: Int, List<Entry>) -> Unit) :
        AsyncTask<Void, Void, List<Entry>>() {
    private var mErrorCode: Int = 0

    override fun doInBackground(vararg params: Void): List<Entry>? {
        try {
            val download = ConnectionHelper.downloadFromUrlByGet(URL)
            val result = JSONObject(download)

            val items = result.getJSONObject("servings")
            val data = ArrayList<Entry>()

            val keys = items.keys()
            while (keys.hasNext()) {
                val key = keys.next() as String

                val date = key.toDate("UTC")
                val ration = items.getInt(key)

                if (date != null)
                    data.add(Entry(date.clearDate().toFloat(), ration.toFloat()))
            }
            Collections.sort(data, ParserHelper.CounterEntryComparator())
            return data
        } catch (e: IOException) {
            mErrorCode = ConnectionHelper.CONNECTION_ERROR
            return null
        } catch (e: JSONException) {
            mErrorCode = ConnectionHelper.INTERNAL_ERROR
            e.printStackTrace()
            return null
        } catch (e: NumberFormatException) {
            mErrorCode = ConnectionHelper.INTERNAL_ERROR
            e.printStackTrace()
            return null
        }
    }

    override fun onPostExecute(result: List<Entry>?) {
        mListener(mErrorCode, result ?: ArrayList() )
    }

    companion object {
        private const val URL = "http://uncmorfi.georgealegre.com/servings"
    }
}
