package com.uncmorfi.balance;

import android.os.AsyncTask;
import android.util.Log;

import com.uncmorfi.balance.model.User;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


class DownloadUserAsyncTask extends AsyncTask<String, Void, User> {
    DownloadUserListener mListener;

    interface DownloadUserListener {
        void onUserDownloaded(User user);
        void onUserRefresh(User user);
        void onUserDownloadFail();
    }

    DownloadUserAsyncTask(DownloadUserListener listener) {
        mListener = listener;
    }

    @Override
    protected User doInBackground(String... params) {
        try {
            String card = params[0];
            String[] tokens = downloadUser(card);

            if (tokens == null || tokens.length < 2)
                return null;

            User user = new User();
            user.setBalance(Integer.parseInt(tokens[6]));
            user.setCard(card);
            user.setName(tokens[17]);
            user.setType(tokens[9]);
            user.setImage(tokens[25]);

            return user;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(User user) {
        if (user == null) mListener.onUserDownloadFail();
        else mListener.onUserDownloaded(user);
    }

    private String[] downloadUser(String card) throws IOException {
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

            // TODO: 11/29/16 crear y llamar al parser
            int left = response.indexOf("rows: [{c: [");
            int rigth = response.indexOf("]", left);
            Log.d("JSwa", "left: " + String.valueOf(left) + " right: " + String.valueOf(rigth));
            String result = response.substring(left + 12, rigth - 2);

            return result.split("['},]*\\{v: '?");

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