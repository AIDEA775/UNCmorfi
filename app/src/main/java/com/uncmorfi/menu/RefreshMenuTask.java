package com.uncmorfi.menu;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Descarga y parsea el menú de la semana.
 */
class RefreshMenuTask extends AsyncTask<Void, Void, String> {
    private static final String URL = "https://www.unc.edu.ar/vida-estudiantil/men%C3%BA-de-la-semana";
    private RefreshMenuListener mListener;

    interface RefreshMenuListener {
        void onRefreshMenuSuccess(String menu);
        void onRefreshMenuFail();
    }

    RefreshMenuTask(RefreshMenuListener listener) {
        mListener = listener;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            Document doc = Jsoup.connect(URL).get();

            // Seleccionar la parte del menú
            Element menu = doc.select("div[property=content:encoded]").first();

            // Quitar la ultima parte
            Elements items = menu.child(0).siblingElements();
            String lastTwo = ":gt(" + String.valueOf(items.size() - 2) + ")";
            items.select(lastTwo).remove();

            return menu.html();
        } catch (IOException t) {
            t.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result == null)
            mListener.onRefreshMenuFail();
        else
            mListener.onRefreshMenuSuccess(result);
    }
}