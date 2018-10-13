package com.uncmorfi.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.view.View
import com.uncmorfi.R

object SnackbarHelper {
    enum class SnackType {
        ERROR,
        LOADING,
        FINISH
    }
}

fun View.snack(context: Context, resId: Int, type: SnackbarHelper.SnackType): Snackbar {
    val bar = Snackbar.make(this, resId, getLength(type))
    setColored(context, bar, type)
    bar.show()
    return bar
}

fun View.snack(context: Context, msg: String, type: SnackbarHelper.SnackType): Snackbar {
    val bar = Snackbar.make(this, msg, getLength(type))
    setColored(context, bar, type)
    bar.show()
    return bar
}

private fun getLength(type: SnackbarHelper.SnackType): Int {
    return when (type) {
        SnackbarHelper.SnackType.ERROR -> Snackbar.LENGTH_INDEFINITE
        SnackbarHelper.SnackType.LOADING -> Snackbar.LENGTH_INDEFINITE
        SnackbarHelper.SnackType.FINISH -> Snackbar.LENGTH_SHORT
    }
}

private fun setColored(context: Context, snackBar: Snackbar, type: SnackbarHelper.SnackType) {
    val color = when (type) {
        SnackbarHelper.SnackType.ERROR -> context.colorOf(R.color.accent)
        SnackbarHelper.SnackType.LOADING -> context.colorOf(R.color.primary_text)
        SnackbarHelper.SnackType.FINISH -> context.colorOf(R.color.primary_dark)
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

// Devuelvo true para obviarlos en los onOptionsItemSelected() de los fragmentos
fun Activity.startBrowser(uri: String) : Boolean {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
    return true
}

fun Activity.shareText(subject: String, text: String) : Boolean {
    val i = Intent(Intent.ACTION_SEND)
    i.type = "text/plain"
    i.putExtra(Intent.EXTRA_SUBJECT, subject)
    i.putExtra(Intent.EXTRA_TEXT, text)
    startActivity(Intent.createChooser(i, "shareText"))
    return true
}