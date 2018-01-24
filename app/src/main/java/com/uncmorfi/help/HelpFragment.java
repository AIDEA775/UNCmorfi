package com.uncmorfi.help;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.uncmorfi.R;


/**
 * Ayuda o FAQ.
 * Muestra una página web alojada en el mismo repositorio de github.
 */
public class HelpFragment extends Fragment {
    private static final String URL = "https://aidea775.github.io/UNCmorfi/resources/help/";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_help, container, false);

        WebView myWebView = v.findViewById(R.id.help_content);
        myWebView.loadUrl(URL);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.navigation_help);
    }
}