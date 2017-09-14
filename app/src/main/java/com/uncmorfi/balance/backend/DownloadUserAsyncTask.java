package com.uncmorfi.balance.backend;

import android.os.AsyncTask;

import com.uncmorfi.balance.model.User;
import com.uncmorfi.helpers.ConnectionHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


class DownloadUserAsyncTask extends AsyncTask<String, Void, User> {
    private DownloadUserListener mListener;
    private int mPosition;
    private DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
    private static final String URL =
            "http://comedor.unc.edu.ar/gv-ds_test.php?json=true&accion=4&codigo=";

    interface DownloadUserListener {
        void onUserDownloaded(User user, int position);
        void onUserDownloadFail();
    }

    DownloadUserAsyncTask(DownloadUserListener listener, int position) {
        mListener = listener;
        mPosition = position;
    }

    @Override
    protected User doInBackground(String... params) {
        try {
            String card = params[0];
            String result = ConnectionHelper.downloadFromUrlByGet(URL + card);

            JSONObject json = new JSONArray(result).getJSONObject(0);

            User user = new User();

            user.setCard(card);
            user.setName(json.getString("nombre"));
            user.setType(json.getString("tipo_cliente"));
            user.setImage(json.getString("foto"));
            user.setBalance(Integer.parseInt(json.getString("saldo")));

            Date expireDate = mDateFormat.parse(json.getString("fecha_hasta"));
            user.setExpiration(expireDate.getTime());

            Date currentTime = Calendar.getInstance().getTime();
            user.setLastUpdate(currentTime.getTime());
            return user;
        } catch (IOException|ParseException|JSONException|NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(User user) {
        if (user == null) mListener.onUserDownloadFail();
        else mListener.onUserDownloaded(user, mPosition);
    }
}