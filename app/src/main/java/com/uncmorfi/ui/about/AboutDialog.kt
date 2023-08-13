package com.uncmorfi.ui.about

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import com.uncmorfi.BuildConfig
import com.uncmorfi.databinding.DialogAboutBinding

class AboutDialog : DialogFragment() {

    private val version: String = BuildConfig.VERSION_NAME

    private lateinit var binding : DialogAboutBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        binding = DialogAboutBinding.inflate(LayoutInflater.from(context),null,false)
        binding.versionName.text = version

        builder.setView(binding.root)
        return builder.create()
    }
}