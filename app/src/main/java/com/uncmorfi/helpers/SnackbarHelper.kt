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

    @JvmStatic
    fun showSnack(context: Context, view: View, resId: Int, type: SnackType): Snackbar {
        val bar = Snackbar.make(view, resId, getLength(type))
        setColored(context, bar, type)
        bar.show()
        return bar
    }

    @JvmStatic
    fun showSnack(context: Context, view: View, msg: String, type: SnackType): Snackbar {
        val bar = Snackbar.make(view, msg, getLength(type))
        setColored(context, bar, type)
        bar.show()
        return bar
    }

    private fun getLength(type: SnackType): Int {
        when (type) {
            SnackbarHelper.SnackType.ERROR -> return Snackbar.LENGTH_INDEFINITE
            SnackbarHelper.SnackType.LOADING -> return Snackbar.LENGTH_INDEFINITE
            SnackbarHelper.SnackType.FINISH -> return Snackbar.LENGTH_SHORT
            else -> return Snackbar.LENGTH_SHORT
        }
    }

    private fun setColored(context: Context, snackbar: Snackbar, type: SnackType) {
        var color = 0
        when (type) {
            SnackbarHelper.SnackType.ERROR -> color = ContextCompat.getColor(context, R.color.accent)
            SnackbarHelper.SnackType.LOADING -> color = ContextCompat.getColor(context, R.color.primary_text)
            SnackbarHelper.SnackType.FINISH -> color = ContextCompat.getColor(context, R.color.primary_dark)
        }
        snackbar.view.setBackgroundColor(color)
    }

}
