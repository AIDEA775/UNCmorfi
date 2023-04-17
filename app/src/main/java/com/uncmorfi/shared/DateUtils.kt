package com.uncmorfi.shared

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

object DateUtils {
    val FORMAT_ARG1: DateTimeFormatter = DateTimeFormatterBuilder() // case insensitive
        .parseCaseInsensitive() // pattern with full month name (MMMM)
        .appendPattern("MMMM yyyy EEEE dd") // default value for day of month
        .parseDefaulting(ChronoField.DAY_OF_MONTH, 1) // default value for hour
        .parseDefaulting(ChronoField.HOUR_OF_DAY, 0) // default value for minute
        .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0) // set locale
        .toFormatter(Locale("es", "ES"))
}