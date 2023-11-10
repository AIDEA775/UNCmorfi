package com.uncmorfi.data.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uncmorfi.data.persistence.entities.Serving
import kotlinx.coroutines.flow.Flow

@Dao
interface ServingDao {
    @Query("SELECT * FROM servings WHERE datetime(date) >= datetime('now', 'start of day') ORDER BY datetime(date)")
    fun getToday(): Flow<List<Serving>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rations: List<Serving>): List<Long>

    @Query("DELETE FROM servings WHERE datetime(date) <= datetime('now','-2 day')")
    suspend fun clearOld()
}