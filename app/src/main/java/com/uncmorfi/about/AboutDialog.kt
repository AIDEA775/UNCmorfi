package com.uncmorfi.about

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.TextView
import com.uncmorfi.BuildConfig
import com.uncmorfi.R

class AboutDialog : DialogFragment() {

    private val versionName: String
        get() {
            return BuildConfig.VERSION_NAME
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        val v = View.inflate(context, R.layout.dialog_about, null)

        val versionText = v.findViewById<TextView>(R.id.version_name)
        versionText.text = versionName

        builder.setView(v)
        return builder.create()
    }
}
