package com.uncmorfi.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.uncmorfi.R

enum class SnackType {
    ERROR,
    LOADING,
    FINISH
}

fun View.snack(context: Context?, resId: Int, type: SnackType): Snackbar {
    val bar = Snackbar.make(this, resId, getLength(type))
    context?.let { setColored(it, bar, type) }
    bar.show()
    return bar
}

fun View.snack(context: Context?, msg: String, type: SnackType): Snackbar {
    val bar = Snackbar.make(this, msg, getLength(type))
    context?.let { setColored(it, bar, type) }
    bar.show()
    return bar
}

fun View.snack(context: Context?, code: ReturnCode): Snackbar {
    val bar = Snackbar.make(this, getMsg(code), getLength(SnackType.ERROR))
    context?.let { setColored(it, bar, SnackType.ERROR) }
    bar.show()
    return bar
}

private fun getMsg(code: ReturnCode): Int {
    return when (code) {
        ReturnCode.INTERNAL_ERROR -> R.string.internal_error
        ReturnCode.CONNECTION_ERROR -> R.string.connection_error
        ReturnCode.OK -> R.string.update_success
    }
}

private fun getLength(type: SnackType): Int {
    return when (type) {
        SnackType.ERROR -> Snackbar.LENGTH_INDEFINITE
        SnackType.LOADING -> Snackbar.LENGTH_INDEFINITE
        SnackType.FINISH -> Snackbar.LENGTH_SHORT
    }
}

private fun setColored(context: Context, snackBar: Snackbar, type: SnackType) {
    val color = when (type) {
        SnackType.ERROR -> context.colorOf(R.color.accent)
        SnackType.LOADING -> context.colorOf(R.color.primary_text)
        SnackType.FINISH -> context.colorOf(R.color.primary_dark)
    }
    snackBar.view.setBackgroundColor(color)
}

fun SwipeRefreshLayout.init(f: () -> Unit) {
    this.setOnRefreshListener { f() }
    this.setProgressBackgroundColorSchemeResource(R.color.accent)
    this.setColorSchemeResources(R.color.white)
}

fun Context.colorOf(resId: Int) : Int {
    return ContextCompat.getColor(this, resId)
}

// Devuelvo true porque lo uso en los onOptionsItemSelected() de los fragments
fun Activity.startBrowser(uri: String) : Boolean {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
    return true
}

fun Activity.shareText(subject: String, text: String, title: String = "UNCmorfi") : Boolean {
    val i = Intent(Intent.ACTION_SEND)
    i.type = "text/plain"
    i.putExtra(Intent.EXTRA_SUBJECT, subject)
    i.putExtra(Intent.EXTRA_TEXT, text)
    startActivity(Intent.createChooser(i, title))
    return true
}

fun EditText.onTextChanged(onTextChanged: (CharSequence) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
            onTextChanged.invoke(s)
        }

        override fun afterTextChanged(editable: Editable?) {
        }
    })
}