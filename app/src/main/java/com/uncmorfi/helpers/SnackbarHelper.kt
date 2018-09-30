package com.uncmorfi.helpers

import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.View

import com.uncmorfi.R

object SnackbarHelper {

    enum class SnackType {
        ERROR,
        LOADING,
        FINISH
    }

    fun showSnack(context: Context, view: View, resId: Int, type: SnackType): Snackbar {
        val bar = Snackbar.make(view, resId, getLength(type))
        setColored(context, bar, type)
        bar.show()
        return bar
    }

    fun showSnack(context: Context, view: View, msg: String, type: SnackType): Snackbar {
        val bar = Snackbar.make(view, msg, getLength(type))
        setColored(context, bar, type)
        bar.show()
        return bar
    }

    private fun getLength(type: SnackType): Int {
        return when (type) {
            SnackbarHelper.SnackType.ERROR -> Snackbar.LENGTH_INDEFINITE
            SnackbarHelper.SnackType.LOADING -> Snackbar.LENGTH_INDEFINITE
            SnackbarHelper.SnackType.FINISH -> Snackbar.LENGTH_SHORT
        }
    }

    private fun setColored(context: Context, snackbar: Snackbar, type: SnackType) {
        val color = when (type) {
            SnackbarHelper.SnackType.ERROR -> ContextCompat.getColor(context, R.color.accent)
            SnackbarHelper.SnackType.LOADING -> ContextCompat.getColor(context, R.color.primary_text)
            SnackbarHelper.SnackType.FINISH -> ContextCompat.getColor(context, R.color.primary_dark)
        }
        snackbar.view.setBackgroundColor(color)
    }

}
