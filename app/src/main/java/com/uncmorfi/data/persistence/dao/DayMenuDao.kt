package com.uncmorfi.data.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uncmorfi.data.persistence.entities.DayMenu
import kotlinx.coroutines.flow.Flow

@Dao
interface DayMenuDao {
    @Query("SELECT * FROM menu ORDER BY datetime(date)")
    suspend fun getAll(): List<DayMenu>

    @Query("SELECT * FROM menu ORDER BY datetime(date)")
    fun getAllAsLiveData(): Flow<List<DayMenu>>

    @Query("SELECT * FROM menu ORDER BY datetime(date) DESC LIMIT 1")
    suspend fun getLast(): DayMenu?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(menus: List<DayMenu>): List<Long>

    @Query("DELETE FROM menu WHERE datetime(date) <= date('now','-30 day')")
    suspend fun clearOld()

    @Query("DELETE FROM menu")
    suspend fun clearAll()
}