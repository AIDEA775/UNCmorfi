package com.uncmorfi.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.uncmorfi.data.network.UserParser
import com.uncmorfi.data.persistence.AppDatabase
import com.uncmorfi.data.persistence.entities.User

class RepoUser(context: Context) {
    private val userDAO = AppDatabase(context).userDao()

    fun getBy(card: String): LiveData<User?> = userDAO.getByCard(card)

    fun getAll() = userDAO.getAllAsLiveData()

    suspend fun fetch(card: String): Int {
        val user = UserParser.fetch(card) ?: return -1

        return userDAO.upsert(listOf(user))
    }

    suspend fun fullUpdate(user: User) {
        userDAO.updateFullUser(user)
    }

    suspend fun delete(user: User) {
        userDAO.delete(user)
    }


}