package com.uncmorfi.ui.faq

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.uncmorfi.R
import com.uncmorfi.databinding.FragmentFaqBinding
import com.uncmorfi.shared.shareText
import com.uncmorfi.shared.startBrowser

/**
 * Preguntas frecuentes.
 * Muestra una página web alojada en el mismo repositorio de github.
 * Depende del lenguaje del sistema operativo.
 */
class FaqFragment : Fragment(R.layout.fragment_faq) {

    private lateinit var binding: FragmentFaqBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFaqBinding.bind(view)
        binding.setUi()
    }

    private fun FragmentFaqBinding.setUi(){
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