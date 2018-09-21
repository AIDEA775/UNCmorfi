package com.uncmorfi.helpers

import com.github.mikephil.charting.data.Entry
import com.uncmorfi.menu.DayMenu
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object ParserHelper {

    fun stringToDate(string: String): Date? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date: Date
        try {
            date = dateFormat.parse(string)
        } catch (e: ParseException) {
            e.printStackTrace()
            return null
        }

        return date
    }

    fun clearDate(date: Date): Long {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.YEAR, 1970)
        cal.set(Calendar.MONTH, 0)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.timeInMillis / 1000
    }

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