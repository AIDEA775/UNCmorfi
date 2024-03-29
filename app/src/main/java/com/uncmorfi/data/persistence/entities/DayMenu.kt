package com.uncmorfi.data.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "menu")
data class DayMenu(
    @PrimaryKey var date: LocalDate = LocalDate.now(),
    val food: List<String> = emptyList()
) {
    fun isToday() = date.isEqual(LocalDate.now())

    override fun toString(): String {
        val menu = food.joinToString()
        return "$date:\n$menu"
    }
}
