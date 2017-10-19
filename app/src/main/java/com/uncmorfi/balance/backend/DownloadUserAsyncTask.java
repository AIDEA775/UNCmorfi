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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Descarga y parsea uno o más usuarios a partir del codigo de la tarjeta.
 */
class DownloadUserAsyncTask extends AsyncTask<String, Void, List<User>> {
    private static final String URL =
            "http://comedor.unc.edu.ar/gv-ds_test.php?json=true&accion=4&codigo=";
    private DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
    private DownloadUserListener mListener;
    private int[] mPosition;
    private int mErrorCode;

    interface DownloadUserListener {
        void onUsersDownloaded(List<User> users);
        void onUsersDownloadFail(int errorCode, int[] positions);
    }

    /**
     * @param position Posiciones de usuarios en el {@link com.uncmorfi.balance.UserCursorAdapter}
     *                 Necesario para actualizar la interfaz de los item de los usuarios.
     */
    DownloadUserAsyncTask(DownloadUserListener listener, int[] position) {
        mListener = listener;
        mPosition = position;
    }

    @Override
    protected List<User> doInBackground(String... params) {
        try {
            String card = params[0];
            String result = ConnectionHelper.downloadFromUrlByGet(URL + card);

            List<User> users = new ArrayList<>();
            JSONArray array = new JSONArray(result);

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                User user = new User();

                user.setCard(item.getString("codigo"));
                user.setName(item.getString("nombre"));
                user.setType(item.getString("tipo_cliente"));
                user.setImage(item.getString("foto"));
                user.setBalance(parseStringToInt(item.getString("saldo")));

                Date expireDate = parseStringToDate(item.getString("fecha_hasta"));
                if (expireDate != null) user.setExpiration(expireDate.getTime());

                Date currentTime = Calendar.getInstance().getTime();
                user.setLastUpdate(currentTime.getTime());

                if (mPosition != null) user.setPosition(mPosition[i]);

                users.add(user);
            }

            return users;
        } catch (IOException e) {
            mErrorCode = ConnectionHelper.CONNECTION_ERROR;
            return null;
        } catch (JSONException e) {
            mErrorCode = ConnectionHelper.INTERNAL_ERROR;
            e.printStackTrace();
            return null;
        }
    }

    private int parseStringToInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
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
    protected void onPostExecute(List<User> users) {
        if (users == null || users.isEmpty())
            mListener.onUsersDownloadFail(mErrorCode, mPosition);
        else
            mListener.onUsersDownloaded(users);
    }

}