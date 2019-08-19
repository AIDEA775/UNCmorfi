package com.uncmorfi.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "servings")
data class Serving (
        @PrimaryKey() var date: Date = Date(),
        val serving: Int = 0
)