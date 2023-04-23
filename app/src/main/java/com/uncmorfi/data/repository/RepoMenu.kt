package com.uncmorfi.data.repository

import android.content.Context
import com.uncmorfi.data.network.MenuParser
import com.uncmorfi.data.persistence.AppDatabase

class RepoMenu(context: Context) {
    private val menuDAO = AppDatabase(context).menuDao()

    fun getAll() = menuDAO.getAllAsLiveData()

    suspend fun update(): List<Long> {
        val menu = MenuParser.fetch()

        if (menu.isEmpty()) {
            return emptyList()
        }
        menuDAO.clearOld()
        return menuDAO.insert(menu)
    }

    suspend fun clear() {
        menuDAO.clearAll()
    }

    suspend fun last() = menuDAO.getLast()

}