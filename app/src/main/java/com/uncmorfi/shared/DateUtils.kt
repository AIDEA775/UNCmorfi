package com.uncmorfi.shared

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

object DateUtils {
    val FORMAT_ARG1: DateTimeFormatter = DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("MMMM yyyy EEEE d")
        .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
        .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
        .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
        .toFormatter(Locale("es", "ES"))

    val FORMAT_ARG2: DateTimeFormatter = DateTimeFormatter.ofPattern("dd")
    val FORMAT_ARG3: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE")
    val FORMAT_ARG4: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:ss:mm")

    val FORMAT_JS: DateTimeFormatter = DateTimeFormatter.ofPattern("'new Date('yyyy',' M',' d')'")
}