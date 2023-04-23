package com.uncmorfi.data.repository

import android.content.Context
import com.uncmorfi.data.network.BalanceParser
import com.uncmorfi.data.persistence.AppDatabase
import com.uncmorfi.data.persistence.entities.User

class RepoUser(context: Context) {
    private val userDAO = AppDatabase(context).userDao()

    fun getAll() = userDAO.getAllAsLiveData()

    suspend fun fetch(card: String): Int {
        val user = BalanceParser.fetch(card) ?: return 0

        return userDAO.upsert(listOf(user))
    }

    suspend fun fullUpdate(user: User) {
        userDAO.updateFullUser(user)
    }


}