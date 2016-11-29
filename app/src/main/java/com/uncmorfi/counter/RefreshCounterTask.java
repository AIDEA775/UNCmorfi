package com.uncmorfi.counter;

import android.os.AsyncTask;

import com.uncmorfi.helpers.ConnectionHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


class RefreshCounterTask extends AsyncTask<Void, Integer, Void> {
    private RefreshCounterListener listener;
    private static final String URL =
            "http://comedor.unc.edu.ar/comedor/1.0/gv-ds.php?accion=1&sede=0475&tqx=reqId:0";

    interface RefreshCounterListener {
        int ANIM_START = 1;
        int ANIM_STOP = 2;
        void onRefreshCounter(int code, int progress);
    }

    RefreshCounterTask(RefreshCounterListener listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            while (!isCancelled()) {
                publishProgress(RefreshCounterListener.ANIM_START, 0);

                int progress = update();
                publishProgress(ConnectionHelper.SUCCESS, progress);

                Thread.sleep(10000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            publishProgress(ConnectionHelper.ERROR, 0);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        listener.onRefreshCounter(values[0], values[1]);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        listener.onRefreshCounter(RefreshCounterListener.ANIM_STOP, 0);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        listener.onRefreshCounter(RefreshCounterListener.ANIM_STOP, 0);
    }

    private Integer update() throws IOException {
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
    }

}
