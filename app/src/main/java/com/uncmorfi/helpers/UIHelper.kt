package com.uncmorfi.helpers

import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.view.View

import com.uncmorfi.R

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
        SnackbarHelper.SnackType.ERROR -> ContextCompat.getColor(context, R.color.accent)
        SnackbarHelper.SnackType.LOADING -> ContextCompat.getColor(context, R.color.primary_text)
        SnackbarHelper.SnackType.FINISH -> ContextCompat.getColor(context, R.color.primary_dark)
    }
    snackBar.view.setBackgroundColor(color)
}

object SnackbarHelper {
    enum class SnackType {
        ERROR,
        LOADING,
        FINISH
    }
}

fun SwipeRefreshLayout.init(f: () -> Unit) {
    this.setOnRefreshListener { f() }
    this.setProgressBackgroundColorSchemeResource(R.color.accent)
    this.setColorSchemeResources(R.color.white)
}
