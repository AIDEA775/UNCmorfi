package com.uncmorfi.data.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.uncmorfi.data.persistence.entities.User
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    suspend fun getAll(): List<User>

    @Query("SELECT * FROM users")
    fun getAllAsLiveData(): LiveData<List<User>>

    @Query("SELECT * FROM users WHERE card IN (:cards)")
    suspend fun getByCard(vararg cards: String): List<User>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(vararg users: User): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(users: List<User>): List<Long>

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
                                  balance: BigDecimal,
                                  expiration: LocalDate,
                                  lastUpdate: Instant
    )

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

        return updateList.size
    }

    /**
     * Inserta o actualiza los usuarios
     * @return la cantidad de filas actualizadas
     */
    @Transaction
    suspend fun upsert(users: List<User>): Int {
        val insertResult = insert(users)
        val updateList = mutableListOf<User>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(users[i])
            }
        }

        for (u in updateList) {
            updatePartialUser(u.card, u.type, u.image, u.balance, u.expiration, u.lastUpdate)
        }

        return updateList.size
    }
}