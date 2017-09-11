package com.uncmorfi.counter;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


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
            InputStream is = null;

            try {
                java.net.URL url = new URL(URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();

                //Get Response
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuilder response = new StringBuilder();
                while((line = rd.readLine()) != null) {
                    response.append(line);
                }
                rd.close();

                String resp = response.toString();

                return new JSONArray(resp);

            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONArray result) {
        super.onPostExecute(result);
        if (result == null) mListener.onRefreshCounterFail();
        else mListener.onRefreshCounterSuccess(result);
    }
}
