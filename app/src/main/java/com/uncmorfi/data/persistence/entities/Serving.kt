package com.uncmorfi.data.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "servings")
data class Serving(
    @PrimaryKey var date: Instant = Instant.now(),
    val serving: Int = 0
)

