package com.uncmorfi.about

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import com.uncmorfi.BuildConfig
import com.uncmorfi.R
import kotlinx.android.synthetic.main.dialog_about.view.*

class AboutDialog : DialogFragment() {

    private val version: String = BuildConfig.VERSION_NAME

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        val v = View.inflate(context, R.layout.dialog_about, null)
        v.versionName.text = version

        builder.setView(v)
        return builder.create()
    }
}
