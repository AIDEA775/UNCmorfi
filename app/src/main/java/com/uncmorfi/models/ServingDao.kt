package com.uncmorfi.models

import androidx.room.*

@Dao
interface ServingDao {
    // Fixme mas de un dia guardado
    @Query("SELECT * FROM servings")
    suspend fun getAll(): List<Serving>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vararg menus: Serving): List<Long>

    @Delete
    suspend fun delete(menu: Serving)
}