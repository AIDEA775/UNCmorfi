package com.uncmorfi.data.repository

import com.uncmorfi.data.network.UserParser
import com.uncmorfi.data.persistence.dao.UserDao
import com.uncmorfi.data.persistence.entities.User
import javax.inject.Inject

class RepoUser @Inject constructor(
    private val userDAO: UserDao,
    private val userParser: UserParser
) {
    fun getBy(card: String) = userDAO.getByCard(card)

    fun getAll() = userDAO.getAll()

    fun listenAll() = userDAO.getAllAsFlow()

    suspend fun fetch(card: String): Int {
        val user = userParser.fetch(card) ?: return -1

        return userDAO.upsert(listOf(user))
    }

    suspend fun fullUpdate(user: User) {
        userDAO.updateFullUser(user)
    }

    suspend fun delete(user: User) {
        userDAO.delete(user)
    }


}