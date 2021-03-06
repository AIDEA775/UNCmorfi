package com.uncmorfi.models

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ServingDao {
    @Query("SELECT * FROM servings WHERE datetime(date) >= datetime('now', 'start of day') ORDER BY datetime(date)")
    suspend fun getToday(): List<Serving>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg menus: Serving): List<Long>

    @Query("DELETE FROM servings WHERE datetime(date) <= datetime('now','-2 day')")
    suspend fun clearOld()
}