package com.uncmorfi.models

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DayMenuDao {
    @Query("SELECT * FROM menu")
    suspend fun getAll(): List<DayMenu>

    @Query("SELECT * FROM menu ORDER BY datetime(date) DESC LIMIT 1")
    suspend fun getLast(): DayMenu?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg menus: DayMenu): List<Long>

    @Query("DELETE FROM menu WHERE datetime(date) <= date('now','-15 day')")
    suspend fun clear()

    @Query("DELETE FROM menu")
    suspend fun clearAll()
}