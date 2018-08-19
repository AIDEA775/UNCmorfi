package com.uncmorfi.balance.backend;

import android.os.AsyncTask;

import com.uncmorfi.balance.model.User;
import com.uncmorfi.helpers.ConnectionHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.net.URL;

/**
 * Descarga y parsea uno o más usuarios a partir del codigo de la tarjeta.
 *
 * Nota Agosto 2018: Durante un tiempo se usó un backend que devolvía los datos en JSON
 * (ver repo sobre Huemul: https://github.com/AIDEA775/Huemul).
 * Ante la posibilidad de poder usar ese formato de nuevo, dejaré el código comentado.
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

            List<User> users = new ArrayList<>();

            // Usando el parseo viejo, sólo podemos procesar un usuario
            users.add(downloadUser(card));

            /*
            String result = ConnectionHelper.downloadFromUrlByGet(URL + card);
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
            */

            return users;
        } catch (Exception e) {
            mErrorCode = ConnectionHelper.INTERNAL_ERROR;
            return null;
        } /*catch (IOException e) {
            mErrorCode = ConnectionHelper.CONNECTION_ERROR;
            return null;
        } catch (JSONException e) {
            mErrorCode = ConnectionHelper.INTERNAL_ERROR;
            e.printStackTrace();
            return null;
        }*/
    }

    private int parseStringToInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
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

    // TODO: 18/08/18 Remover el parseo viejo.
    private User downloadUser(String card) {
        String urlParameters = "accion=4&responseHandler=setDatos&codigo=" + card;

        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL("http://comedor.unc.edu.ar/gv-ds.php");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", "" +
                    Integer.toString(urlParameters.getBytes().length));

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            int left = response.indexOf("rows: [{c: [");
            int right = response.indexOf("]", left);
            String result = response.substring(left + 12, right - 2);

            String[] tokens = result.split("['},]*\\{v: '?");

            if (tokens.length < 2)
                return null;

            User user = new User();
            try {
                user.setBalance(Integer.parseInt(tokens[6]));
            } catch (NumberFormatException e) {
                user.setBalance(0);
            }
            user.setCard(card);
            user.setName(tokens[17]);
            user.setType(tokens[9]);
            user.setImage(tokens[25]);

            Date expireDate = parseStringToDate(tokens[5]);
            if (expireDate != null) user.setExpiration(expireDate.getTime());

            Date currentTime = Calendar.getInstance().getTime();
            user.setLastUpdate(currentTime.getTime());

            if (mPosition != null) user.setPosition(mPosition[0]);

            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}