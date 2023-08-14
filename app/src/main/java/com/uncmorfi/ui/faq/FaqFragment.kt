package com.uncmorfi.ui.faq

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.uncmorfi.R
import com.uncmorfi.databinding.FragmentFaqBinding
import com.uncmorfi.shared.addMenu
import com.uncmorfi.shared.shareText
import com.uncmorfi.shared.startBrowser

/**
 * Preguntas frecuentes.
 * Muestra una página web alojada en el mismo repositorio de github.
 * Depende del lenguaje del sistema operativo.
 */
class FaqFragment : Fragment(R.layout.fragment_faq) {

    private lateinit var binding: FragmentFaqBinding

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

        addMenu(R.menu.faq){menuItemId ->
            when(menuItemId){
                R.id.faq_share -> requireActivity().shareText("UNCmorfi FAQ", URL)
                R.id.faq_browser -> requireActivity().startBrowser(URL)
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_faq)
    }

    companion object {
        private const val URL = "https://aidea775.github.io/UNCmorfi"
    }
}