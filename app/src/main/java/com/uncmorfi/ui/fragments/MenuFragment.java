package com.uncmorfi.ui.fragments;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import com.uncmorfi.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MenuFragment extends Fragment {
    // Para datos
    private String menuHtml;

    // UI
    private WebView webView;
    SwipeRefreshLayout mSwipeRefreshLayout;

    // Colores
    //noinspection ResourceType
    private String colorDays;
    //noinspection ResourceType
    private String colorAll;

    // Tarea asincrona
    AsyncTask thread;

    final String file = "menu.txt";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //noinspection ResourceType
        colorDays = getContext().getString(R.color.primary_text).substring(3);
        //noinspection ResourceType
        colorAll = getContext().getString(R.color.secondary_text).substring(3);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout para este Fragment
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        // Recuperar el menú
        getMenu();

        // UI
        webView = (WebView) view.findViewById(R.id.menu_text);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.loadData(menuHtml, "text/html","UTF-8");

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.menu_swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshMenu();
                    }
                }
        );

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() ==  R.id.action_sync_menu) {
            // Actualizar el menú
            refreshMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshMenu() {
        // Chequear conexión a internet
        ConnectivityManager connMgr = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // Iniciar tarea asincrona si hay conexión
        if (networkInfo != null && networkInfo.isConnected()) {
            thread = new ParseMenu().execute();
        } else {
            Toast.makeText(getContext(),
                    getContext().getString(R.string.no_connection), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void getMenu() {
        // Si le menú no está cargado, leer desde la memoria interna
        if (menuHtml == null) {
            try {
                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(
                                        getContext().openFileInput(file)));
                String line;
                StringBuilder read = new StringBuilder();
                while((line = in.readLine()) != null) {
                    read.append(line);
                }
                in.close();
                menuHtml = read.toString();
            } catch (Exception ex) {
                Log.e("Files", "Error reading in memory");
            }
        }
    }

    public void saveMenu(String menu) {
        // No escribir nada si menu es vacio
        if (menu == null)
            return;

        // Escribir en la memoria interna
        try {
            OutputStreamWriter out=
                    new OutputStreamWriter(
                            getContext().openFileOutput(file, Context.MODE_PRIVATE));

            out.write(menu);
            out.close();
        } catch(IOException ex) {
            Log.e("Files", "Error writing in memory");
        }
    }


    private class ParseMenu extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            mSwipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                // Descargar el html
                Document doc = Jsoup.connect("http://m.unc.edu.ar/vidaestudiantil/sae/comedor/menu").get();

                // Seleccionar la parte del menú
                Element menu = doc.getElementById("content-core").child(0).child(0);

                // Seleccionar el texto en negrita
                Elements bold = menu.select("strong");

                // Colorear como texto primario
                bold.wrap("<font size=4 color=\"#" + colorDays + "\"></font>");

                // Quitar la ultima parte
                menu.child(0).lastElementSibling().remove();

                return "<html><head><style type=\"text/css\">body{color: #" + colorAll +
                        "}</style></head><body>" + menu.html().replaceAll("•[ &nbsp;]*", "&#128523; ")
                        + "</body></html>";
            } catch (Throwable t) {
                t.printStackTrace();
                this.cancel(true);
            }
            return null;
        }
        @Override
        protected void onCancelled() {
            Toast.makeText(getContext(),
                getContext().getString(R.string.connection_error), Toast.LENGTH_SHORT)
                .show();
            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        protected void onPostExecute(String result) {
            // Mostrar el texto con formato
            if (result != null && webView != null) {
                webView.loadData(result, "text/html","UTF-8");
                saveMenu(result);
                menuHtml = result;

                Toast.makeText(getContext(),
                        getContext().getString(R.string.refresh_success), Toast.LENGTH_SHORT)
                        .show();
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }

    }

}