package com.uncmorfi.helpers;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.uncmorfi.R;

public abstract class SnackbarHelper {

    public enum SnackType {
        ERROR,
        LOADING,
        FINISH
    }

    public static Snackbar showSnack(Context context, View view, int resId, SnackType type) {
        Snackbar bar = Snackbar.make(view, resId, getLength(type));
        setColored(context, bar, type);
        bar.show();
        return bar;
    }

    public static Snackbar showSnack(Context context, View view, String msg, SnackType type) {
        Snackbar bar = Snackbar.make(view, msg, getLength(type));
        setColored(context, bar, type);
        bar.show();
        return bar;
    }

    private static int getLength(SnackType type) {
        switch (type) {
            case ERROR:
                return Snackbar.LENGTH_INDEFINITE;
            case LOADING:
                return Snackbar.LENGTH_INDEFINITE;
            case FINISH:
                return Snackbar.LENGTH_SHORT;
            default:
                return Snackbar.LENGTH_SHORT;
        }
    }

    private static void setColored(Context context, Snackbar snackbar, SnackType type) {
        int color = 0;
        switch (type) {
            case ERROR:
                color = ContextCompat.getColor(context, R.color.accent);
                break;
            case LOADING:
                color = ContextCompat.getColor(context, R.color.primary_text);
                break;
            case FINISH:
                color = ContextCompat.getColor(context, R.color.primary_dark);
                break;
        }
        snackbar.getView().setBackgroundColor(color);
    }

}
