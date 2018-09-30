package com.uncmorfi.faq

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import com.uncmorfi.R


/**
 * Preguntas frecuentes.
 * Muestra una página web alojada en el mismo repositorio de github.
 * Depende del lenguaje del sistema operativo.
 */
class FaqFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_faq, container, false)
        val webView = v.findViewById<WebView>(R.id.faq_content)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(URL)
        return v
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_faq)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.faq, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.faq_share) {
            val i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            i.putExtra(Intent.EXTRA_SUBJECT, "UNCmorfi FAQ")
            i.putExtra(Intent.EXTRA_TEXT, URL)
            startActivity(Intent.createChooser(i, "share"))
            return true
        } else if (item.itemId == R.id.faq_browser) {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(URL))
            startActivity(i)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val URL = "https://aidea775.github.io/UNCmorfi"
    }
}