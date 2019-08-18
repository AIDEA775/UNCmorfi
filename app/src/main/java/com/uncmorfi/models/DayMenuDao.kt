package com.uncmorfi.models

import androidx.room.*

@Dao
interface DayMenuDao {
    @Query("SELECT * FROM menu")
    suspend fun getAll(): List<DayMenu>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vararg menus: DayMenu): List<Long>

    @Delete
    suspend fun delete(menu: DayMenu)
}