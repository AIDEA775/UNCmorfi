package com.uncmorfi.fragments;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.uncmorfi.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class CounterFragment extends Fragment {
    // Toolbar
    MenuItem refreshItem;

    // UI
    private TextView resume;
    private ProgressBar bar;
    private TextView percent;

    // Tarea asincrona
    private AsyncTask thread;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // todo crear un fab para actualizar
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout para este Fragment
        View view = inflater.inflate(R.layout.fragment_counter, container, false);

        resume = (TextView) view.findViewById(R.id.counter_resume);
        bar = (ProgressBar) view.findViewById(R.id.counter_bar);
        percent = (TextView) view.findViewById(R.id.counter_percent);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.counter, menu);
        refreshItem = menu.findItem(R.id.action_sync_counter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sync_counter) {
            // Actualizar el menú
            refreshCounter();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (thread != null) {
            thread.cancel(true);
        }
        animationStop();
    }

    private void refreshCounter() {
        // chequear conexión a internet
        ConnectivityManager connMgr = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // Iniciar tarea asincrona si hay conexión
        if (networkInfo != null && networkInfo.isConnected()) {
            this.thread = new ParseCounter().execute();
        } else {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.no_connection),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void animationStart() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.icon_refresh, null);

        Animation rotation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);

        refreshItem.setActionView(iv);
    }

    private void animationStop() {
        if (refreshItem.getActionView() != null) {
            refreshItem.getActionView().clearAnimation();
            refreshItem.setActionView(null);
        }
    }


    private class ParseCounter extends AsyncTask<Void, Integer, Void> {
        final String URL = "http://comedor.unc.edu.ar/comedor/1.0/gv-ds.php?accion=1&sede=0475&tqx=reqId:0";

        @Override
        protected void onPreExecute() {
            bar.setMax(1500);
            bar.setProgress(0);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                while (!isCancelled()) {
                    // Iniciar animacion
                    publishProgress(-1);

                    // Actualizar datos
                    int progress = update();

                    // Actualizar UI
                    publishProgress(progress);

                    // Esperar 10 segundos
                    Thread.sleep(10000);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                publishProgress(-2);
                this.cancel(true);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] == -1) {
                animationStart();
            } else if (values[0] == -2) {
                Toast.makeText(getContext(),
                        getContext().getString(R.string.connection_error), Toast.LENGTH_SHORT)
                        .show();
            } else {
                animationStop();

                // Actualizar UI
                bar.setProgress(values[0]);
                resume.setText(String.format(Locale.US, "%d raciones de %d", values[0], 1500));
                percent.setText(String.format(Locale.US, "%d%%", (values[0]*100)/1500));

                Toast.makeText(getContext(),
                        getContext().getString(R.string.refresh_success), Toast.LENGTH_SHORT)
                        .show();
            }
        }

        @Override
        protected void onCancelled() {
            animationStop();
        }

        private Integer update() throws IOException {
            InputStream is = null;

            try {
                URL url = new URL(URL);
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
                    is.close();
                }
            }
        }

    }

}