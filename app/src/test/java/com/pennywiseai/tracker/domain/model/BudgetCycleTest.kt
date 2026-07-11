package com.pennywiseai.tracker.domain.model

import com.pennywiseai.tracker.utils.JalaliYearMonth
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/**
 * Locks down the budget-cycle math that the Home, Budget Groups, and widget surfaces
 * rely on. The cycle doesn't have to align with the calendar month — when a user's
 * salary lands on the 25th, the cycle is the 25th of one month through the 24th of
 * the next. These tests pin the exact window for the cases the spec calls out, plus
 * the clamp behaviour for short months (Feb in leap and non-leap years).
 */
class BudgetCycleTest {

    @Test
    fun `startDay 1 behaves like the calendar month`() {
        val today = LocalDate.of(2026, 10, 5)
        val (start, end) = BudgetCycle.currentCycle(today, startDay = 1)

        assertEquals(LocalDate.of(2026, 10, 1), start)
        assertEquals(LocalDate.of(2026, 10, 31), end)
    }

    @Test
    fun `startDay 25 today Oct 5 spans Sep 25 through Oct 24`() {
        val today = LocalDate.of(2026, 10, 5)
        val (start, end) = BudgetCycle.currentCycle(today, startDay = 25)

        assertEquals(LocalDate.of(2026, 9, 25), start)
        assertEquals(LocalDate.of(2026, 10, 24), end)
    }

    @Test
    fun `startDay 25 today Oct 26 spans Oct 25 through Nov 24`() {
        val today = LocalDate.of(2026, 10, 26)
        val (start, end) = BudgetCycle.currentCycle(today, startDay = 25)

        assertEquals(LocalDate.of(2026, 10, 25), start)
        assertEquals(LocalDate.of(2026, 11, 24), end)
    }

    @Test
    fun `startDay 29 in non-leap February falls back to Jan 29 through Feb 27`() {
        val today = LocalDate.of(2025, 2, 10)
        val (start, end) = BudgetCycle.currentCycle(today, startDay = 29)

        // Feb 10 is before the Feb 28 candidate, so the cycle started in January.
        // Next cycle begins Feb 28, so this cycle ends the day before (Feb 27).
        assertEquals(LocalDate.of(2025, 1, 29), start)
        assertEquals(LocalDate.of(2025, 2, 27), end)
    }

    @Test
    fun `startDay 29 in leap February falls back to Jan 29 through Feb 28`() {
        val today = LocalDate.of(2024, 2, 10)
        val (start, end) = BudgetCycle.currentCycle(today, startDay = 29)

        // Feb 10 is before the Feb 29 candidate, so the cycle started in January.
        // Next cycle begins Feb 29, so this cycle ends Feb 28.
        assertEquals(LocalDate.of(2024, 1, 29), start)
        assertEquals(LocalDate.of(2024, 2, 28), end)
    }

    @Test
    fun `startDay 31 today Feb 15 non-leap is Jan 31 through Feb 27`() {
        val today = LocalDate.of(2025, 2, 15)
        val (start, end) = BudgetCycle.currentCycle(today, startDay = 31)

        assertEquals(LocalDate.of(2025, 1, 31), start)
        assertEquals(LocalDate.of(2025, 2, 27), end)
    }

    @Test
    fun `startDay 31 today Feb 15 leap year is Jan 31 through Feb 28`() {
        val today = LocalDate.of(2024, 2, 15)
        val (start, end) = BudgetCycle.currentCycle(today, startDay = 31)

        assertEquals(LocalDate.of(2024, 1, 31), start)
        assertEquals(LocalDate.of(2024, 2, 28), end)
    }

    @Test
    fun `currentCycleStartYearMonth returns September for startDay 25 today Oct 5`() {
        val today = LocalDate.of(2026, 10, 5)
        val ym = BudgetCycle.currentCycleStartYearMonth(today, startDay = 25)

        assertEquals(YearMonth.of(2026, 9), ym)
    }

    @Test
    fun `currentCycleStartYearMonth returns October for startDay 25 today Oct 26`() {
        val today = LocalDate.of(2026, 10, 26)
        val ym = BudgetCycle.currentCycleStartYearMonth(today, startDay = 25)

        assertEquals(YearMonth.of(2026, 10), ym)
    }

    @Test
    fun `clampStartDay clamps 0 to 1 and 32 to 31`() {
        assertEquals(1, BudgetCycle.clampStartDay(0))
        assertEquals(31, BudgetCycle.clampStartDay(32))
        assertEquals(15, BudgetCycle.clampStartDay(15))
    }

    @Test
    fun `previousCycle steps back exactly one cycle`() {
        val today = LocalDate.of(2026, 10, 5)
        val current = BudgetCycle.currentCycle(today, startDay = 25)
        val (prevStart, prevEnd) = BudgetCycle.previousCycle(current, startDay = 25)

        assertEquals(LocalDate.of(2026, 8, 25), prevStart)
        assertEquals(LocalDate.of(2026, 9, 24), prevEnd)
    }

    // ── Jalali (Persian) mode ──

    @Test
    fun `useJalali startDay 1 spans the Jalali calendar month, not the Gregorian one`() {
        // 2026-07-11 falls in Jalali month Tir 1405, a 31-day month running
        // 2026-06-22 through 2026-07-22 inclusive.
        val today = LocalDate.of(2026, 7, 11)
        val (start, end) = BudgetCycle.currentCycle(today, startDay = 1, useJalali = true)

        val expectedStart = JalaliYearMonth.from(today).atDay(1)
        val expectedEnd = JalaliYearMonth.from(today).atEndOfMonth()
        assertEquals(expectedStart, start)
        assertEquals(expectedEnd, end)
        // Sanity: this must NOT equal the Gregorian calendar month.
        assertEquals(LocalDate.of(2026, 6, 22), start)
        assertEquals(LocalDate.of(2026, 7, 22), end)
    }

    @Test
    fun `useJalali startDay 10 rolls to the previous Jalali month when today is before day 10`() {
        // 1 Tir 1405 = 2026-06-22 (Jalali month start). Day 5 of that month is before
        // startDay 10, so the cycle should still be running from the *previous*
        // Jalali month's day 10.
        val today = LocalDate.of(2026, 6, 26) // 5 Tir 1405
        val (start, _) = BudgetCycle.currentCycle(today, startDay = 10, useJalali = true)

        val expectedStart = JalaliYearMonth.from(today).minusMonths(1).atDay(10)
        assertEquals(expectedStart, start)
    }

    @Test
    fun `useJalali defaults to false so existing Gregorian callers are unaffected`() {
        val today = LocalDate.of(2026, 10, 5)
        val gregorian = BudgetCycle.currentCycle(today, startDay = 1)
        val explicitGregorian = BudgetCycle.currentCycle(today, startDay = 1, useJalali = false)

        assertEquals(gregorian, explicitGregorian)
    }

    @Test
    fun `useJalali previousCycle and nextCycleStart mirror the Gregorian cadence in Jalali months`() {
        val today = LocalDate.of(2026, 7, 11)
        val current = BudgetCycle.currentCycle(today, startDay = 1, useJalali = true)
        val (prevStart, prevEnd) = BudgetCycle.previousCycle(current, startDay = 1, useJalali = true)

        assertEquals(JalaliYearMonth.from(today).minusMonths(1).atDay(1), prevStart)
        assertEquals(current.first.minusDays(1), prevEnd)
    }
}
