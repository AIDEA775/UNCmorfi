package com.uncmorfi.counter;

import android.os.AsyncTask;

import com.github.mikephil.charting.data.Entry;
import com.uncmorfi.helpers.ConnectionHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *  Descarga y parsea los datos obtenidos del medidor de raciones.
 */
class RefreshCounterTask extends AsyncTask<Void, Void, List<Entry>> {
    private static final String URL =
            "http://comedor.unc.edu.ar/gv-ds_test.php?json=true&accion=1&sede=0475' and cast(fecha as date) = date '2017-5-9' group by 1 order by 1 -- hola";
    private RefreshCounterListener mListener;
    private DateFormat mDateFormat = new SimpleDateFormat("HH:mm", Locale.ROOT);
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
            JSONArray result = new JSONArray(download);
            List<Entry> data = new ArrayList<>();

            for (int i = 0; i < result.length(); i++) {
                    JSONObject item = result.getJSONObject(i);

                    Date date = parseStringToDate(item.getString("fecha"));
                    int ration = Integer.parseInt(item.getString("raciones"));

                    if (date != null)
                        data.add(new Entry(date.getTime(), ration));
            }
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

    private Date parseStringToDate(String string) {
        try {
            return mDateFormat.parse(string);
        } catch (ParseException e) {
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
