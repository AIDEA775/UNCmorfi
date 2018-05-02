package com.uncmorfi.faq;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_faq, container, false);

        WebView webView = v.findViewById(R.id.faq_content);

        String language = Locale.getDefault().getLanguage();
        webView.loadUrl(String.format(URL, language));

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.navigation_faq);
    }
}