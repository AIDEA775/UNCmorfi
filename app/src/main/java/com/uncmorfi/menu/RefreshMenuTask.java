package com.uncmorfi.menu;

import android.content.Context;
import android.os.AsyncTask;

import com.uncmorfi.helpers.ConnectionHelper;
import com.uncmorfi.helpers.MemoryHelper;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import static com.uncmorfi.menu.MenuFragment.MENU_FILE;

/**
 * Descarga y parsea el menú de la semana.
 */
class RefreshMenuTask extends AsyncTask<Void, Void, List<DayMenu>> {
    private static final String URL = "http://uncmorfi.georgealegre.com/menu";
    private RefreshMenuListener mListener;
    private WeakReference<Context> mContext;

    interface RefreshMenuListener {
        void onRefreshMenuSuccess(List<DayMenu> menu);
        void onRefreshMenuFail();
    }

    RefreshMenuTask(Context context, RefreshMenuListener listener) {
        mContext = new WeakReference<>(context);
        mListener = listener;
    }

    @Override
    protected List<DayMenu> doInBackground(Void... params) {
        try {
            String download = ConnectionHelper.downloadFromUrlByGet(URL);

            Context context = mContext.get();
            if (context != null && needSaveMenu(context, download) ) {
                MemoryHelper.saveToStorage(context, MENU_FILE, download);
            }

            return DayMenu.fromJson(download);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<DayMenu> result) {
        if (result == null)
            mListener.onRefreshMenuFail();
        else
            mListener.onRefreshMenuSuccess(result);
    }

    private boolean needSaveMenu(Context context, String menu) {
        String menuSaved = MemoryHelper.readHeadFromStorage(context, MENU_FILE);
        return (menuSaved == null || !menu.startsWith(menuSaved));
    }
}