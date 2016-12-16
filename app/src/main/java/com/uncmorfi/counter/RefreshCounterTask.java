package com.uncmorfi.counter;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


class RefreshCounterTask extends AsyncTask<Void, Void, Integer> {
    private RefreshCounterListener mListener;
    private static final String URL =
            "http://comedor.unc.edu.ar/comedor/1.0/gv-ds.php?accion=1&sede=0475&tqx=reqId:0";

    interface RefreshCounterListener {
        void onRefreshCounterSuccess(int percent);
        void onRefreshCounterFail();
    }

    RefreshCounterTask(RefreshCounterListener listener) {
        mListener = listener;
    }

    @Override
    protected Integer doInBackground(Void... params) {
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

                // TODO: 11/29/16 crear y llamar al parser que devuele un HashMap
                // Buscar las columnas con datos
                int left = resp.indexOf("rows: [");
                int right = resp.lastIndexOf("]");

                try {
                    resp = resp.substring(left + 17, right - 3);
                } catch (StringIndexOutOfBoundsException e) {
                    return 0;
                }

                // Dividir los datos
                // response tiene la forma:
                // 12:04:00'},{v: 12}]},...{c: [{v: 'hora'},{v: num}]},...{c: [{v: '13:16:00'},{v: 4
                String[] tokens = resp.split("[\\}\\]']*\\},\\{[c: \\[\\{v']*");

                // Calcular el total
                // tokens tiene la forma: ["12:04:00", "12", ... , "hora", "num"]
                int result = 0;
                for (int i = 1; i < tokens.length; i+=2) {
                    result += Integer.parseInt(tokens[i]);
                }
                return result;

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
    }

    @Override
    protected void onPostExecute(Integer percent) {
        super.onPostExecute(percent);
        if (percent == null) mListener.onRefreshCounterFail();
        else mListener.onRefreshCounterSuccess(percent);
    }
}
