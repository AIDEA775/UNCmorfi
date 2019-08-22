package com.uncmorfi.models

import androidx.room.TypeConverter
import com.uncmorfi.helpers.toCalendar
import com.uncmorfi.helpers.toISOString
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: String?): Calendar? {
        return value?.toCalendar()
    }

    @TypeConverter
    fun dateToTimestamp(cal: Calendar?): String? {
        return cal?.toISOString()
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