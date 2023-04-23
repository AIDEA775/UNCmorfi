package com.uncmorfi.data.persistence

import androidx.room.TypeConverter
import com.uncmorfi.shared.toCalendar
import com.uncmorfi.shared.toISOString
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
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
    fun readLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(value) }
    }

    @TypeConverter
    fun saveLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun readInstant(value: String?): Instant? {
        return value?.let { Instant.parse(value) }
    }

    @TypeConverter
    fun saveInstant(value: Instant?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun readBigDecimal(value: String?): BigDecimal? {
        return value?.let { BigDecimal(value) }
    }

    @TypeConverter
    fun saveBigDecimal(value: BigDecimal?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun listToString(value: List<String>?): String {
        return value?.joinToString("*-*") ?: ""
    }

    @TypeConverter
    fun stringToList(value: String): List<String> {
        return value.split("*-*")
    }
}