package com.uncmorfi.helpers;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public abstract class ConnectionHelper {
    public static final int CONNECTION_ERROR = -2;
    public static final int ERROR = -1;
    public static final int SUCCESS = 0;


    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
