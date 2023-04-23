package com.uncmorfi.shared

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializer
import org.json.JSONArray
import java.io.IOException
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

fun String.toCalendar(timeZone: String? = null): Calendar? {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    val calendar = Calendar.getInstance()
    val date: Date
    timeZone?.let { dateFormat.timeZone = TimeZone.getTimeZone(it) }

    return try {
        date = dateFormat.parse(this)
        calendar.time = date
        calendar
    } catch (e: ParseException) {
        e.printStackTrace()
        null
    }
}

fun Calendar.toISOString(): String {
    val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    return fmt.format(this.time)
}

fun Calendar.clearDate(): Long {
    this.set(Calendar.YEAR, 1970)
    this.set(Calendar.MONTH, 0)
    this.set(Calendar.DAY_OF_MONTH, 1)
    return this.timeInMillis / 1000
}

fun Calendar.toFormat(format: String) : String {
    val fmt = SimpleDateFormat(format, Locale.getDefault())
    return fmt.format(this.time)
}

fun Calendar.compareToToday(): Int {
    val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    return fmt.format(this.time).compareTo(fmt.format(Date()))
}

fun Calendar.compareToTodayInMillis(): Int {
    return this.compareTo(Calendar.getInstance())
}

class CalendarDeserializer : JsonDeserializer<Calendar> {
    @Throws(IOException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type,
                             context: JsonDeserializationContext): Calendar {
        return json.asString.toCalendar()!!

    }
}