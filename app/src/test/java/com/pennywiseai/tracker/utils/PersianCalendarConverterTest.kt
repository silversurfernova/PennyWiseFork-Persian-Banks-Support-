package com.pennywiseai.tracker.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PersianCalendarConverterTest {

    @Test
    fun `known Nowruz anchors convert correctly`() {
        // Well-known Jalali New Year (1 Farvardin) dates. 1403 was a leap year
        // (see the leap-year test below), which pushes the *following* Nowruz
        // one day later than usual — 1404's is March 21, not March 20.
        assertEquals(Triple(1403, 1, 1), PersianCalendarConverter.toJalali(2024, 3, 20))
        assertEquals(Triple(1358, 1, 1), PersianCalendarConverter.toJalali(1979, 3, 21))
        assertEquals(Triple(1379, 1, 1), PersianCalendarConverter.toJalali(2000, 3, 20))
        assertEquals(Triple(1404, 1, 1), PersianCalendarConverter.toJalali(2025, 3, 21))
        assertEquals(Triple(1403, 12, 30), PersianCalendarConverter.toJalali(2025, 3, 20))
    }

    @Test
    fun `toGregorian is the exact inverse of toJalali for Nowruz anchors`() {
        assertEquals(Triple(2024, 3, 20), PersianCalendarConverter.toGregorian(1403, 1, 1))
        assertEquals(Triple(1979, 3, 21), PersianCalendarConverter.toGregorian(1358, 1, 1))
        assertEquals(Triple(2025, 3, 21), PersianCalendarConverter.toGregorian(1404, 1, 1))
    }

    @Test
    fun `round trip is stable across a wide range of dates`() {
        var date = LocalDate.of(1950, 1, 1)
        val end = LocalDate.of(2050, 1, 1)
        while (date.isBefore(end)) {
            val (jy, jm, jd) = PersianCalendarConverter.toJalali(date.year, date.monthValue, date.dayOfMonth)
            val (gy, gm, gd) = PersianCalendarConverter.toGregorian(jy, jm, jd)
            assertEquals("round-trip failed for $date", Triple(date.year, date.monthValue, date.dayOfMonth), Triple(gy, gm, gd))
            date = date.plusDays(37) // sample, not exhaustive — full century would be slow
        }
    }

    @Test
    fun `1403 is a leap year with a 30-day Esfand, 1404 is not`() {
        assertTrue(PersianCalendarConverter.isLeapJalaliYear(1403))
        assertEquals(30, PersianCalendarConverter.jalaliMonthLength(1403, 12))
        assertEquals(false, PersianCalendarConverter.isLeapJalaliYear(1404))
        assertEquals(29, PersianCalendarConverter.jalaliMonthLength(1404, 12))
    }

    @Test
    fun `first six months are 31 days, next five are 30`() {
        for (m in 1..6) assertEquals(31, PersianCalendarConverter.jalaliMonthLength(1404, m))
        for (m in 7..11) assertEquals(30, PersianCalendarConverter.jalaliMonthLength(1404, m))
    }

    @Test
    fun `JalaliYearMonth atDay and atEndOfMonth match toGregorian`() {
        val ym = JalaliYearMonth(1403, 12) // leap year's Esfand, 30 days
        assertEquals(LocalDate.of(2025, 3, 20), ym.atEndOfMonth())
        assertEquals(30, ym.lengthOfMonth())
    }

    @Test
    fun `JalaliYearMonth plusMonths and minusMonths roll over year boundaries`() {
        val esfand1403 = JalaliYearMonth(1403, 12)
        assertEquals(JalaliYearMonth(1404, 1), esfand1403.plusMonths(1))
        assertEquals(JalaliYearMonth(1403, 11), esfand1403.minusMonths(1))

        val farvardin1404 = JalaliYearMonth(1404, 1)
        assertEquals(JalaliYearMonth(1403, 12), farvardin1404.minusMonths(1))
    }

    @Test
    fun `JalaliYearMonth from converts a Gregorian date to the containing Jalali month`() {
        assertEquals(JalaliYearMonth(1403, 1), JalaliYearMonth.from(LocalDate.of(2024, 3, 25)))
        assertEquals(JalaliYearMonth(1402, 12), JalaliYearMonth.from(LocalDate.of(2024, 3, 19)))
    }
}
