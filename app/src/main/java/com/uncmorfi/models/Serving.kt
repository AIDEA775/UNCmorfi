package com.uncmorfi.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "servings")
data class Serving (
        @PrimaryKey var date: Calendar = Calendar.getInstance(),
        val serving: Int = 0
)

data class Servings (
        val servings: Map<Calendar, Int>
)