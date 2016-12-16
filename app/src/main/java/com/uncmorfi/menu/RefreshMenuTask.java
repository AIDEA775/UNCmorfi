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
            Document doc = Jsoup.connect("http://m.unc.edu.ar/vidaestudiantil/sae/comedor/menu")
                    .get();

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
            String result = "<html><head><style type=\"text/css\">body{color: #757575" +
                    "}</style></head><body>" + body + "</body></html>";

            MemoryHelper.saveToInternalMemory(mContext, MenuFragment.MENU_FILE, result);
            return result;
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