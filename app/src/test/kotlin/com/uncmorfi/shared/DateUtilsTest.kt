package com.uncmorfi.shared

import com.uncmorfi.shared.DateUtils.delayToNext
import org.junit.Assert
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

class DateUtilsTest {

    @Test
    fun `delay from 5-30 to 8-00`() {
        val now = LocalDateTime.of(2023, 1, 1, 5, 30)
        val out = delayToNext(LocalTime.of(8, 0), now)

        Assert.assertEquals(out, Duration.ofMinutes(150))
    }

    @Test
    fun `delay from 7-59 to 8-00`() {
        val now = LocalDateTime.of(2023, 1, 1, 7, 59)
        val out = delayToNext(LocalTime.of(8, 0), now)

        Assert.assertEquals(out, Duration.ofMinutes(1))
    }

    @Test
    fun `delay from 13 to 8`() {
        val now = LocalDateTime.of(2023, 1, 1, 13, 0)
        val out = delayToNext(LocalTime.of(8, 0), now)

        Assert.assertEquals(out, Duration.ofHours(19))
    }

    @Test
    fun `delay from 20 to 8`() {
        val now = LocalDateTime.of(2023, 1, 1, 20, 0)
        val out = delayToNext(LocalTime.of(8, 0), now)

        Assert.assertEquals(out, Duration.ofHours(12))
    }

    @Test
    fun `delay from 00 to 8`() {
        val now = LocalDateTime.of(2023, 1, 1, 0, 0)
        val out = delayToNext(LocalTime.of(8, 0), now)

        Assert.assertEquals(out, Duration.ofHours(8))
    }
}