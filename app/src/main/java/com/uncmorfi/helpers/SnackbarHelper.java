package com.uncmorfi.helpers;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;

import com.uncmorfi.R;


public abstract class SnackbarHelper {
    public enum SnackType {
        ERROR,
        LOADING,
        FINISH
    }

    public static int getLength(SnackType type) {
        switch (type) {
            case ERROR:
                return Snackbar.LENGTH_INDEFINITE;
            case LOADING:
                return Snackbar.LENGTH_INDEFINITE;
            case FINISH:
                return Snackbar.LENGTH_SHORT;
        }
        return Snackbar.LENGTH_SHORT;
    }

    public static Snackbar getColored(Context context, Snackbar snackbar, SnackType type) {
        int color = 0;
        switch (type) {
            case ERROR:
                color = ContextCompat.getColor(context, R.color.accent);
                break;
            case LOADING:
                color = ContextCompat.getColor(context, R.color.secondary_text);
                break;
            case FINISH:
                color = ContextCompat.getColor(context, R.color.primary_dark);
                break;
        }
        snackbar.getView().setBackgroundColor(color);
        return snackbar;
    }
}
