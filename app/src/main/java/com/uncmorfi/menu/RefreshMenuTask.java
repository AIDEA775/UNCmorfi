package com.uncmorfi.menu;

import android.content.Context;
import android.os.AsyncTask;

import com.uncmorfi.helpers.MemoryHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


class RefreshMenuTask extends AsyncTask<Void, Void, String> {
    private static final String URL = "https://www.unc.edu.ar/vida-estudiantil/menú-de-la-semana";
    private RefreshMenuListener mListener;
    private Context mContext;

    interface RefreshMenuListener {
        void onRefreshMenuSuccess(String menu);
        void onRefreshMenuFail();
    }

    RefreshMenuTask(Context context, RefreshMenuListener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            // Descargar el html
            Document doc = Jsoup.connect(URL).get();

            // Seleccionar la parte del menú
            Element menu = doc.select("div[property=content:encoded]").first();

            // Quitar la ultima parte
            Elements items = menu.child(0).siblingElements();
            String lastTwo = ":gt(" + String.valueOf(items.size() - 2) + ")";
            items.select(lastTwo).remove();

            String body = menu.html();

            MemoryHelper.saveToInternalMemory(mContext, MenuFragment.MENU_FILE, body);

            return body;
        } catch (IOException t) {
            t.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result == null) mListener.onRefreshMenuFail();
        else mListener.onRefreshMenuSuccess(result);
    }
}