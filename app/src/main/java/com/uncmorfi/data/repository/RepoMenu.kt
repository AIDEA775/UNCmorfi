package com.uncmorfi.data.repository

import com.uncmorfi.data.network.MenuParser
import com.uncmorfi.data.persistence.dao.DayMenuDao
import javax.inject.Inject

class RepoMenu @Inject constructor(
    private val menuDAO : DayMenuDao
) {
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