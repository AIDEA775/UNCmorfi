package com.uncmorfi.menu

import android.content.Context
import android.os.AsyncTask

import com.uncmorfi.helpers.ConnectionHelper
import com.uncmorfi.helpers.MemoryHelper

import java.io.IOException
import java.lang.ref.WeakReference

/**
 * Descarga y parsea el menú de la semana.
 */
internal class RefreshMenuTask(context: Context, private val mListener: RefreshMenuListener) :
        AsyncTask<Void, Void, List<DayMenu>>() {
    private val mContext: WeakReference<Context> = WeakReference(context)

    internal interface RefreshMenuListener {
        fun onRefreshMenuSuccess(menu: List<DayMenu>)
        fun onRefreshMenuFail()
    }

    override fun doInBackground(vararg params: Void): List<DayMenu>? {
        val download: String
        try {
            download = ConnectionHelper.downloadFromUrlByGet(URL)

            val context = mContext.get()
            if (context != null && needSaveMenu(context, download)) {
                MemoryHelper.saveToStorage(context, MenuFragment.MENU_FILE, download)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        return DayMenu.fromJson(download)
    }

    override fun onPostExecute(result: List<DayMenu>?) {
        if (result == null)
            mListener.onRefreshMenuFail()
        else
            mListener.onRefreshMenuSuccess(result)
    }

    private fun needSaveMenu(context: Context, menu: String): Boolean {
        val menuSaved = MemoryHelper.readHeadFromStorage(context, MenuFragment.MENU_FILE)
        return menuSaved == null || !menu.startsWith(menuSaved)
    }

    companion object {
        private const val URL = "http://uncmorfi.georgealegre.com/menu"
    }
}