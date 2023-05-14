package com.uncmorfi.data.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.uncmorfi.data.persistence.entities.User
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE card = :card")
    fun getByCard(card: String): LiveData<User?>

    @Query("SELECT * FROM users")
    fun getAllAsLiveData(): LiveData<List<User>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(users: List<User>): List<Long>

    @Update
    suspend fun updateFullUser(vararg users: User)

    @Query(
        """
        UPDATE users SET
        type = :type,
        email = :email,
        image = :image,
        balance = :balance,
        price = :price,
        rations = :rations,
        expiration = :expiration,
        lastUpdate = :lastUpdate
        WHERE card = :card
        """
    )
    suspend fun updatePartialUser(
        card: String,
        type: String?,
        email: String,
        image: String?,
        balance: BigDecimal?,
        price: BigDecimal?,
        rations: Int?,
        expiration: LocalDate,
        lastUpdate: Instant
    )

    @Delete
    suspend fun delete(user: User)

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
            updatePartialUser(
                u.card,
                u.type,
                u.email,
                u.image,
                u.balance,
                u.price,
                u.rations,
                u.expiration,
                u.lastUpdate
            )
        }

        return updateList.size
    }
}