package com.uncmorfi.counter;

import android.os.AsyncTask;

import com.github.mikephil.charting.data.Entry;
import com.uncmorfi.helpers.ConnectionHelper;
import com.uncmorfi.helpers.ParserHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *  Descarga y parsea los datos obtenidos del medidor de raciones.
 */
class RefreshCounterTask extends AsyncTask<Void, Void, List<Entry>> {
    private static final String URL = "http://uncmorfi.georgealegre.com/servings";
    private RefreshCounterListener mListener;
    private int mErrorCode;

    interface RefreshCounterListener {
        void onRefreshCounterSuccess(List<Entry> result);
        void onRefreshCounterFail(int errorCode);
    }

    RefreshCounterTask(RefreshCounterListener listener) {
        mListener = listener;
    }

    @Override
    protected List<Entry> doInBackground(Void... params) {
        try {
            String download = ConnectionHelper.downloadFromUrlByGet(URL);
            JSONObject result = new JSONObject(download);

            JSONObject items = result.getJSONObject("servings");
            List<Entry> data = new ArrayList<>();

            Iterator<?> keys = items.keys();
            while(keys.hasNext()) {
                String key = (String) keys.next();
                Date date = ParserHelper.stringToDate(key);
                int ration =  items.getInt(key);

                if (date != null)
                    data.add(new Entry(date.getTime(), ration));
            }
            Collections.sort(data, new ParserHelper.CounterEntryComparator());
            return data;
        } catch (IOException e) {
            mErrorCode = ConnectionHelper.CONNECTION_ERROR;
            return null;
        } catch (JSONException|NumberFormatException e) {
            mErrorCode = ConnectionHelper.INTERNAL_ERROR;
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Entry> result) {
        super.onPostExecute(result);
        if (result == null)
            mListener.onRefreshCounterFail(mErrorCode);
        else
            mListener.onRefreshCounterSuccess(result);
    }
}
