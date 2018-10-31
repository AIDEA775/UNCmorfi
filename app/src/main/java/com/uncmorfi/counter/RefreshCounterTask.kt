package com.uncmorfi.counter

import android.os.AsyncTask
import com.github.mikephil.charting.data.Entry
import com.uncmorfi.helpers.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.*

/**
 * Descarga y parsea los datos obtenidos del medidor de raciones.
 */
internal class RefreshCounterTask(private val mListener: (code: ReturnCode, List<Entry>) -> Unit) :
        AsyncTask<Void, Void, List<Entry>>() {
    private var mErrorCode: ReturnCode = ReturnCode.OK

    override fun doInBackground(vararg params: Void): List<Entry>? {
        try {
            val download = URL(url).downloadByGet()
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
            mErrorCode = ReturnCode.CONNECTION_ERROR
            return null
        } catch (e: JSONException) {
            mErrorCode = ReturnCode.INTERNAL_ERROR
            e.printStackTrace()
            return null
        } catch (e: NumberFormatException) {
            mErrorCode = ReturnCode.INTERNAL_ERROR
            e.printStackTrace()
            return null
        }
    }

    override fun onPostExecute(result: List<Entry>?) {
        mListener(mErrorCode, result ?: ArrayList() )
    }

    companion object {
        private const val url = "http://uncmorfi.georgealegre.com/servings"
    }
}
