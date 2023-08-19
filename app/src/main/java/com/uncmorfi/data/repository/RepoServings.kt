package com.uncmorfi.data.repository

import com.uncmorfi.data.network.ServingParser
import com.uncmorfi.data.persistence.dao.ServingDao
import javax.inject.Inject

class RepoServings @Inject constructor(
    private val servingDAO: ServingDao,
    private val servingParser: ServingParser
) {
    fun getToday() = servingDAO.getToday()

    suspend fun update(): List<Long> {
        val servings = servingParser.fetch()

        if (servings.isEmpty()) {
            return emptyList()
        }

        servingDAO.clearOld()
        return servingDAO.insert(servings)
    }
}