package com.uncmorfi.shared

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.io.IOException
import java.lang.reflect.Type
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
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

class CalendarDeserializer : JsonDeserializer<Calendar> {
    @Throws(IOException::class)
    override fun deserialize(
        json: JsonElement, typeOfT: Type,
        context: JsonDeserializationContext
    ): Calendar {
        return json.asString.toCalendar()!!

    }
}

fun BigDecimal.toMoneyFormat(): String = NumberFormat.getCurrencyInstance().format(this)

fun String.toBigDecimalOrZero(): BigDecimal = toBigDecimalOrNull() ?: BigDecimal.ZERO