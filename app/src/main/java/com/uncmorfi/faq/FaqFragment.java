package com.uncmorfi.faq;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.uncmorfi.R;

import java.util.Locale;


/**
 * Preguntas frecuentes.
 * Muestra una página web alojada en el mismo repositorio de github.
 * Depende del lenguaje del sistema operativo.
 */
public class FaqFragment extends Fragment {
    private static final String URL = "https://aidea775.github.io/UNCmorfi/faq/index-%s.html";
    private String mUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_faq, container, false);
        WebView webView = v.findViewById(R.id.faq_content);

        mUrl = String.format(URL, Locale.getDefault().getLanguage());

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(mUrl);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.navigation_faq);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.faq, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() ==  R.id.faq_share) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "UNCmorfi FAQ");
            i.putExtra(Intent.EXTRA_TEXT, mUrl);
            startActivity(Intent.createChooser(i, "share"));
            return true;
        } else if (item.getItemId() == R.id.faq_browser) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}