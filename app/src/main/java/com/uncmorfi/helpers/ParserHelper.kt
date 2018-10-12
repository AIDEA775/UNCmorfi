package com.uncmorfi.helpers

import com.github.mikephil.charting.data.Entry
import com.uncmorfi.menu.DayMenu
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

fun String.toDate(timeZone: String? = null): Date? {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    timeZone?.let { dateFormat.timeZone = TimeZone.getTimeZone(it) }
    val date: Date
    try {
        date = dateFormat.parse(this)
    } catch (e: ParseException) {
        e.printStackTrace()
        return null
    }

    return date
}

fun Date.clearDate(): Long {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(Calendar.YEAR, 1970)
    cal.set(Calendar.MONTH, 0)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    return cal.timeInMillis / 1000
}

fun Date.toString(format: String) : String {
    val fmt = SimpleDateFormat(format, Locale.getDefault())
    return fmt.format(this)
}

object ParserHelper {
    class MenuDayComparator : Comparator<DayMenu> {
        override fun compare(left: DayMenu, right: DayMenu): Int {
            return left.date?.compareTo(right.date) ?: -1
        }
    }

    class CounterEntryComparator : Comparator<Entry> {
        override fun compare(left: Entry, right: Entry): Int {
            return Date(left.x.toLong()).compareTo(Date(right.x.toLong()))
        }
    }
}