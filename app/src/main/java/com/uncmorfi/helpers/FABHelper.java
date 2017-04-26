package com.uncmorfi.helpers;

import android.content.Context;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;


public abstract class FABHelper {
    public static void showFAB(Context context, FloatingActionButton fab) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Interpolator interpolator = AnimationUtils.loadInterpolator(context,
                    android.R.interpolator.fast_out_slow_in);
            if (fab.getScaleX() == 0) {
                fab.animate()
                        .scaleX(1)
                        .scaleY(1)
                        .setInterpolator(interpolator)
                        .setDuration(600);
            }
        } else {
            fab.show();
        }
    }

    public static void hideFAB(Context context, FloatingActionButton fab) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Interpolator interpolator = AnimationUtils.loadInterpolator(context,
                    android.R.interpolator.fast_out_slow_in);
            if (fab.getScaleX() == 1) {
                fab.animate()
                        .scaleX(0)
                        .scaleY(0)
                        .setInterpolator(interpolator)
                        .setDuration(300);
            }
        } else {
            fab.hide();
        }
    }
}
