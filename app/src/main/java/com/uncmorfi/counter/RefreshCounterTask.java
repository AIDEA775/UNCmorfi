package com.uncmorfi.counter;

import android.os.AsyncTask;

import com.uncmorfi.helpers.ConnectionHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;


class RefreshCounterTask extends AsyncTask<Void, Void, JSONArray> {
    private RefreshCounterListener mListener;
    private static final String URL =
            "http://comedor.unc.edu.ar/gv-ds_test.php?json=true&accion=1&sede=0475";

    interface RefreshCounterListener {
        void onRefreshCounterSuccess(JSONArray result);
        void onRefreshCounterFail();
    }

    RefreshCounterTask(RefreshCounterListener listener) {
        mListener = listener;
    }

    @Override
    protected JSONArray doInBackground(Void... params) {
        try {
            String result = ConnectionHelper.downloadFromUrlByGet(URL);
            return new JSONArray(result);
        } catch (IOException|JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(JSONArray result) {
        super.onPostExecute(result);
        if (result == null) mListener.onRefreshCounterFail();
        else mListener.onRefreshCounterSuccess(result);
    }
}
