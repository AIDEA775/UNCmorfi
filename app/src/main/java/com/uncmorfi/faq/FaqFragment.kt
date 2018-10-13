package com.uncmorfi.faq

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.webkit.WebViewClient
import com.uncmorfi.R
import com.uncmorfi.helpers.startBrowser
import com.uncmorfi.helpers.shareText
import kotlinx.android.synthetic.main.fragment_faq.*

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_faq, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        faqContent.webViewClient = WebViewClient()
        faqContent.settings.javaScriptEnabled = true
        faqContent.loadUrl(URL)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_faq)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.faq, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.faq_share -> requireActivity().shareText("UNCmorfi FAQ", URL)
            R.id.faq_browser -> requireActivity().startBrowser(URL)
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val URL = "https://aidea775.github.io/UNCmorfi"
    }
}