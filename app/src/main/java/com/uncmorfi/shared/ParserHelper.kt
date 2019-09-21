package com.uncmorfi.shared

import com.uncmorfi.models.DayMenu
import com.uncmorfi.models.Serving
import org.json.JSONArray
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun JSONArray.toArray() : Array<String> {
    val result = ArrayList<String>()
    for (i in 0 until this.length()) {
        result.add(this.getString(i))
    }
    return result.toTypedArray()
}

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

object ParserHelper {
    class MenuDayComparator : Comparator<DayMenu> {
        override fun compare(left: DayMenu, right: DayMenu): Int {
            return left.date.compareTo(right.date)
        }
    }

    class ServingsComparator : Comparator<Serving> {
        override fun compare(left: Serving, right: Serving): Int {
            return left.date.compareTo(right.date)
        }
    }
}