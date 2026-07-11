package com.pennywiseai.tracker.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * App-wide date *display* formatting. Reads [useJalaliCalendar], pushed from
 * PennyWiseApplication at startup (same @Volatile pattern as
 * [CurrencyFormatter.numberFormatStyle] — a plain object can't collect a
 * DataStore Flow itself, and this is read from Composables all over the app,
 * not scoped to one screen's ViewModel).
 *
 * Switches only how dates are *shown*; storage, parsing, filtering, and
 * backups are untouched and stay Gregorian.
 */
object DateFormatter {

    @Volatile
    var useJalaliCalendar: Boolean = false

    private val dayMonthFormatter = DateTimeFormatter.ofPattern("d MMM")
    private val dayMonthYearFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    /** "21 Jul" / "۲۱ تیر" */
    fun formatDayMonth(date: LocalDate): String =
        if (useJalaliCalendar) PersianDateFormatter.formatShort(date)
        else date.format(dayMonthFormatter)

    /** "21 Jul 2026" / "۲۱ تیر ۱۴۰۴" */
    fun formatDayMonthYear(date: LocalDate): String =
        if (useJalaliCalendar) PersianDateFormatter.formatFull(date)
        else date.format(dayMonthYearFormatter)

    /** "21 Jul · 3:45 PM" / "۲۱ تیر · ۳:۴۵ ب.ظ" — only the date part converts. */
    fun formatDayMonthTime(dateTime: LocalDateTime): String {
        val datePart = formatDayMonth(dateTime.toLocalDate())
        val timePart = if (useJalaliCalendar) PersianDateFormatter.toPersianDigits(dateTime.format(timeFormatter))
            else dateTime.format(timeFormatter)
        return "$datePart · $timePart"
    }

    private val dayMonthYearWeekdayFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")

    /** "Sun, Jul 21, 2026 · 3:45 PM" / "یکشنبه، ۲۱ تیر ۱۴۰۴ · ۳:۴۵ ب.ظ" */
    fun formatDayMonthYearTime(dateTime: LocalDateTime): String {
        val datePart = if (useJalaliCalendar) PersianDateFormatter.formatFullWithWeekday(dateTime.toLocalDate())
            else dateTime.format(dayMonthYearWeekdayFormatter)
        val timePart = if (useJalaliCalendar) PersianDateFormatter.toPersianDigits(dateTime.format(timeFormatter))
            else dateTime.format(timeFormatter)
        return "$datePart · $timePart"
    }
}

/**
 * Jalali (Persian) rendering of a [LocalDate] — native month names and digits,
 * the natural way dates are read in Iran. Pure formatting; the actual
 * calendar math lives in [PersianCalendarConverter].
 */
object PersianDateFormatter {

    private val monthNames = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(input: String): String =
        input.map { c -> if (c in '0'..'9') persianDigits[c - '0'] else c }.joinToString("")

    /** "۲۱ تیر" (day + month, no year — for list rows) */
    fun formatShort(date: LocalDate): String {
        val (_, jm, jd) = PersianCalendarConverter.toJalali(date.year, date.monthValue, date.dayOfMonth)
        return toPersianDigits("$jd ${monthNames[jm - 1]}")
    }

    /** "۲۱ تیر ۱۴۰۴" (day + month + year) */
    fun formatFull(date: LocalDate): String {
        val (jy, jm, jd) = PersianCalendarConverter.toJalali(date.year, date.monthValue, date.dayOfMonth)
        return toPersianDigits("$jd ${monthNames[jm - 1]} $jy")
    }

    private val weekdayNames = mapOf(
        java.time.DayOfWeek.SATURDAY to "شنبه",
        java.time.DayOfWeek.SUNDAY to "یکشنبه",
        java.time.DayOfWeek.MONDAY to "دوشنبه",
        java.time.DayOfWeek.TUESDAY to "سه‌شنبه",
        java.time.DayOfWeek.WEDNESDAY to "چهارشنبه",
        java.time.DayOfWeek.THURSDAY to "پنج‌شنبه",
        java.time.DayOfWeek.FRIDAY to "جمعه"
    )

    /** "یکشنبه، ۲۱ تیر ۱۴۰۴" (weekday + day + month + year) */
    fun formatFullWithWeekday(date: LocalDate): String {
        val (jy, jm, jd) = PersianCalendarConverter.toJalali(date.year, date.monthValue, date.dayOfMonth)
        val weekday = weekdayNames[date.dayOfWeek].orEmpty()
        return toPersianDigits("$weekday، $jd ${monthNames[jm - 1]} $jy")
    }
}
