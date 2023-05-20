package com.uncmorfi.data.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.ZoneId

@Entity(tableName = "servings")
data class Serving(
    @PrimaryKey var date: Instant = Instant.now(),
    val serving: Int = 0
) {

    fun toFloat() = date.atZone(ZoneId.systemDefault())
        .toLocalTime()
        .toSecondOfDay()
        .toFloat()

    override fun toString(): String {
        return "Serving(date=$date, x=${toFloat()}, serving=$serving)"
    }
}

