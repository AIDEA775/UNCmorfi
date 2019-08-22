package com.uncmorfi.models

import androidx.room.*
import java.util.*

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    suspend fun getAll(): List<User>

    @Query("SELECT * FROM users WHERE card IN (:cards)")
    suspend fun getByCard(vararg cards: String): List<User>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(vararg users: User): List<Long>

    @Update
    suspend fun updateFullUser(vararg users: User)

    @Query("""
        UPDATE users SET
        type = :type,
        image = :image,
        balance= :balance,
        expiration= :expiration,
        lastUpdate= :lastUpdate
        WHERE card = :card
        """)
    suspend fun updatePartialUser(card: String,
                                  type: String?,
                                  image: String?,
                                  balance: Int,
                                  expiration: Calendar,
                                  lastUpdate: Calendar)

    @Delete
    suspend fun delete(user: User)

    // Inserta o actualiza los usuarios
    // Devuelve la cantidad de filas afectadas
    @Transaction
    suspend fun upsertUser(vararg users: User): Int {
        val insertResult = insertUser(*users)
        val updateList = mutableListOf<User>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(users[i])
            }
        }

        for (u in updateList) {
            updatePartialUser(u.card, u.type, u.image, u.balance, u.expiration, u.lastUpdate)
        }

        return updateList.count()
    }
}