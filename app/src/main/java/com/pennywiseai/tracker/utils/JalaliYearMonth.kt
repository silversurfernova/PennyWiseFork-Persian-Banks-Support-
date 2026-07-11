package com.pennywiseai.tracker.utils

import java.time.LocalDate

/**
 * Jalali (Persian) analogue of [java.time.YearMonth] — a (year, month) pair with
 * month-length-aware navigation. Mirrors the subset of YearMonth's API that
 * [com.pennywiseai.tracker.domain.model.BudgetCycle] and [com.pennywiseai.tracker.presentation.common.getDateRangeForPeriod]
 * need, so the Jalali branch of that logic can read as a straight swap of
 * `YearMonth` for `JalaliYearMonth` instead of bespoke arithmetic.
 */
data class JalaliYearMonth(val year: Int, val month: Int) : Comparable<JalaliYearMonth> {

    init {
        require(month in 1..12) { "Jalali month must be 1..12, was $month" }
    }

    override fun compareTo(other: JalaliYearMonth): Int =
        compareValuesBy(this, other, { it.year }, { it.month })

    fun lengthOfMonth(): Int = PersianCalendarConverter.jalaliMonthLength(year, month)

    fun plusMonths(months: Long): JalaliYearMonth {
        val total = (year.toLong() * 12 + (month - 1)) + months
        val newYear = Math.floorDiv(total, 12L).toInt()
        val newMonth = Math.floorMod(total, 12L).toInt() + 1
        return JalaliYearMonth(newYear, newMonth)
    }

    fun minusMonths(months: Long): JalaliYearMonth = plusMonths(-months)

    /** Gregorian [LocalDate] for [day] (1-based) within this Jalali month. */
    fun atDay(day: Int): LocalDate {
        val (gy, gm, gd) = PersianCalendarConverter.toGregorian(year, month, day)
        return LocalDate.of(gy, gm, gd)
    }

    /** Gregorian [LocalDate] of the last day of this Jalali month. */
    fun atEndOfMonth(): LocalDate = atDay(lengthOfMonth())

    companion object {
        fun from(date: LocalDate): JalaliYearMonth {
            val (jy, jm, _) = PersianCalendarConverter.toJalali(date.year, date.monthValue, date.dayOfMonth)
            return JalaliYearMonth(jy, jm)
        }
    }
}
