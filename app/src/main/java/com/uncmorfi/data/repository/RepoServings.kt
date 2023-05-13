package com.uncmorfi.data.repository

import android.content.Context
import com.uncmorfi.data.network.ServingParser
import com.uncmorfi.data.persistence.AppDatabase

class RepoServings(context: Context) {
    private val servingDAO = AppDatabase(context).servingDao()

    fun getToday() = servingDAO.getToday()

    suspend fun update(): List<Long> {
        val servings = ServingParser.fetch()

        if (servings.isNullOrEmpty()) {
            return emptyList()
        }

        servingDAO.clearOld()
        return servingDAO.insert(servings)
    }
}