package com.uncmorfi.counter

import android.os.AsyncTask
import com.github.mikephil.charting.data.Entry
import com.uncmorfi.helpers.ConnectionHelper
import com.uncmorfi.helpers.ParserHelper
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

/**
 * Descarga y parsea los datos obtenidos del medidor de raciones.
 */
internal class RefreshCounterTask(private val mListener: RefreshCounterListener) :
        AsyncTask<Void, Void, List<Entry>>() {
    private var mErrorCode: Int = 0

    internal interface RefreshCounterListener {
        fun onRefreshCounterSuccess(result: List<Entry>)
        fun onRefreshCounterFail(errorCode: Int)
    }

    override fun doInBackground(vararg params: Void): List<Entry>? {
        try {
            val download = ConnectionHelper.downloadFromUrlByGet(URL)
            val result = JSONObject(download)

            val items = result.getJSONObject("servings")
            val data = ArrayList<Entry>()

            val keys = items.keys()
            while (keys.hasNext()) {
                val key = keys.next() as String

                val date = ParserHelper.stringToDate(key)
                val ration = items.getInt(key)

                if (date != null)
                    data.add(Entry(ParserHelper.clearDate(date).toFloat(), ration.toFloat()))
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
        super.onPostExecute(result)
        if (result == null)
            mListener.onRefreshCounterFail(mErrorCode)
        else
            mListener.onRefreshCounterSuccess(result)
    }

    companion object {
        private const val URL = "http://uncmorfi.georgealegre.com/servings"
    }
}
