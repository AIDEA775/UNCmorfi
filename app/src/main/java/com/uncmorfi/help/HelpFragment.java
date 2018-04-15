package com.uncmorfi.help;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.uncmorfi.R;

import java.util.Locale;


/**
 * Ayuda o FAQ.
 * Muestra una página web alojada en el mismo repositorio de github.
 */
public class HelpFragment extends Fragment {
    private static final String URL = "https://aidea775.github.io/UNCmorfi/help/index-%s.html";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_help, container, false);

        WebView webView = v.findViewById(R.id.help_content);

        String language = Locale.getDefault().getLanguage();
        webView.loadUrl(String.format(URL, language));

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.navigation_help);
    }
}