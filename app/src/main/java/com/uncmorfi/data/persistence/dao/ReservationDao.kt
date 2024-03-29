package com.uncmorfi.data.persistence.dao

import androidx.room.*
import com.uncmorfi.data.persistence.entities.Cookie
import com.uncmorfi.data.persistence.entities.Reservation


@Dao
interface ReservationDao {

    @Query("SELECT * FROM reservations WHERE code LIKE :code")
    suspend fun getReservation(code: String): Reservation?

    @Query("SELECT * FROM cookies WHERE code_id LIKE :code")
    suspend fun getCookies(code: String): List<Cookie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReserves(vararg reserve: Reservation)

    @Insert
    suspend fun insertCookies(vararg cookies: Cookie)

    @Transaction
    suspend fun insert(reserve: Reservation) {
        insertReserves(reserve)
        deleteCookies(reserve.code)
        insertCookies(*reserve.cookies!!.toTypedArray())
    }

    @Query("DELETE FROM reservations WHERE code LIKE :code")
    suspend fun delete(code: String)

    @Query("DELETE FROM cookies WHERE code_id LIKE :code")
    suspend fun deleteCookies(code: String)
}
