package com.uncmorfi.menu;

import android.os.AsyncTask;

import com.uncmorfi.helpers.ConnectionHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


class RefreshMenuTask extends AsyncTask<Void, Void, String> {
    private RefreshMenuListener listener;


    interface RefreshMenuListener {
        void onNewMenuDownloaded(int code, String menu);
    }

    RefreshMenuTask(RefreshMenuListener listener) {
        this.listener = listener;
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
            bold.wrap("<font size=4 color=\"#212121\"></font>");

            // Quitar la ultima parte
            menu.child(0).lastElementSibling().remove();

            // Reemplazar simbolo del item y espacios vacios
            String body = menu.html().replaceAll("•[ &nbsp;]*", "&#128523; ");

            // Agregar encabezado y color al texto
            return "<html><head><style type=\"text/css\">body{color: #757575" +
                    "}</style></head><body>" + body + "</body></html>";
        } catch (IOException t) {
            t.printStackTrace();
            return null;
        }
    }
    @Override
    protected void onCancelled() {
        listener.onNewMenuDownloaded(ConnectionHelper.ERROR, null);
    }

    @Override
    protected void onPostExecute(String result) {
        listener.onNewMenuDownloaded(result == null?
                ConnectionHelper.CONNECTION_ERROR : ConnectionHelper.SUCCESS, result);
    }

}