package com.uncmorfi.models

import androidx.room.TypeConverter
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun listToString(value: List<String>?): String {
        return value?.joinToString("*-*") ?: ""
    }

    @TypeConverter
    fun stringToList(value: String): List<String>? {
        return value.split("*-*")
    }
}