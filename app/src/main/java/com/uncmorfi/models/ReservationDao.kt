package com.uncmorfi.models

import androidx.room.*


@Dao
interface ReservationDao {

    @Query("SELECT * FROM reservations WHERE code LIKE :code")
    suspend fun getReservation(code: String): Reservation

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

    @Delete
    suspend fun delete(reserve: Reservation)

    @Query("DELETE FROM cookies WHERE code_id LIKE :code")
    suspend fun deleteCookies(code: String)
}
