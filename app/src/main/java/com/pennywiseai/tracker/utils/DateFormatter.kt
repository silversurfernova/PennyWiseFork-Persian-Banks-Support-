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
 * backups are untouched and stay Gregorian. Month names are Latin
 * transliterations ("Ordibehesht"), not Persian script — mixing RTL script
 * into otherwise-LTR rows (e.g. a transaction list item) triggers bidi
 * reordering that visibly breaks the layout, so this keeps every rendered
 * string plain LTR regardless of calendar mode.
 */
object DateFormatter {

    @Volatile
    var useJalaliCalendar: Boolean = false

    private val dayMonthFormatter = DateTimeFormatter.ofPattern("d MMM")
    private val dayMonthYearFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    private val yearFormatter = DateTimeFormatter.ofPattern("yyyy")
    private val monthFormatter = DateTimeFormatter.ofPattern("MMM")
    private val monthYearShortFormatter = DateTimeFormatter.ofPattern("MMM yy")
    private val dayMonthYearWeekdayFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")

    /** "21 Jul" / "21 Tir" */
    fun formatDayMonth(date: LocalDate): String =
        if (useJalaliCalendar) PersianDateFormatter.formatShort(date)
        else date.format(dayMonthFormatter)

    /** "21 Jul 2026" / "21 Tir 1405" */
    fun formatDayMonthYear(date: LocalDate): String =
        if (useJalaliCalendar) PersianDateFormatter.formatFull(date)
        else date.format(dayMonthYearFormatter)

    /** "21 Jul · 3:45 PM" / "21 Tir · 3:45 PM" — only the date part converts. */
    fun formatDayMonthTime(dateTime: LocalDateTime): String {
        val datePart = formatDayMonth(dateTime.toLocalDate())
        val timePart = dateTime.format(timeFormatter)
        return "$datePart · $timePart"
    }

    /** "Sun, Jul 21, 2026 · 3:45 PM" / "Yekshanbe, 21 Tir 1405 · 3:45 PM" */
    fun formatDayMonthYearTime(dateTime: LocalDateTime): String {
        val datePart = if (useJalaliCalendar) PersianDateFormatter.formatFullWithWeekday(dateTime.toLocalDate())
            else dateTime.format(dayMonthYearWeekdayFormatter)
        val timePart = dateTime.format(timeFormatter)
        return "$datePart · $timePart"
    }

    /** Bare year, e.g. "2026" / "1405". For the edit-mode date button and yearly chart labels. */
    fun formatYear(date: LocalDate): String =
        if (useJalaliCalendar) PersianDateFormatter.yearOnly(date) else date.format(yearFormatter)

    /** Month name only, e.g. "Jul" / "Tir". For monthly chart/heatmap labels. */
    fun formatMonth(date: LocalDate): String =
        if (useJalaliCalendar) PersianDateFormatter.monthOnly(date) else date.format(monthFormatter)

    /** Month name + 2-digit year, e.g. "Jul 26" / "Tir 05". For charts spanning multiple years. */
    fun formatMonthYear(date: LocalDate): String =
        if (useJalaliCalendar) PersianDateFormatter.monthYearShort(date) else date.format(monthYearShortFormatter)

    /** The calendar year [date] falls in, in whichever system is active — for "does this span multiple years" checks. */
    fun calendarYear(date: LocalDate): Int =
        if (useJalaliCalendar) PersianCalendarConverter.toJalali(date.year, date.monthValue, date.dayOfMonth).first
        else date.year

    /** The calendar month (1-12) [date] falls in, in whichever system is active. */
    fun calendarMonthValue(date: LocalDate): Int =
        if (useJalaliCalendar) PersianCalendarConverter.toJalali(date.year, date.monthValue, date.dayOfMonth).second
        else date.monthValue

    /** True if [date] is the 1st day of its calendar year, in whichever system is active. */
    fun isYearStart(date: LocalDate): Boolean = isMonthStart(date) && calendarMonthValue(date) == 1

    /** True if [date] is the 1st day of its calendar month, in whichever system is active. */
    fun isMonthStart(date: LocalDate): Boolean =
        if (useJalaliCalendar) PersianCalendarConverter.toJalali(date.year, date.monthValue, date.dayOfMonth).third == 1
        else date.dayOfMonth == 1
}

/**
 * Jalali (Persian) rendering of a [LocalDate] — Latin-transliterated month/weekday
 * names ("Ordibehesht", not "اردیبهشت") and plain digits. Deliberately avoids
 * Persian script and Persian numerals: embedding RTL script in an otherwise-LTR
 * string (a transaction row's "date · time") triggers Unicode bidi reordering
 * that visibly scrambles the layout. Pure formatting; the actual calendar math
 * lives in [PersianCalendarConverter].
 */
object PersianDateFormatter {

    private val monthNames = arrayOf(
        "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
        "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand"
    )

    private val weekdayNames = mapOf(
        java.time.DayOfWeek.SATURDAY to "Shanbe",
        java.time.DayOfWeek.SUNDAY to "Yekshanbe",
        java.time.DayOfWeek.MONDAY to "Doshanbe",
        java.time.DayOfWeek.TUESDAY to "Seshanbe",
        java.time.DayOfWeek.WEDNESDAY to "Chaharshanbe",
        java.time.DayOfWeek.THURSDAY to "Panjshanbe",
        java.time.DayOfWeek.FRIDAY to "Jomeh"
    )

    /** "21 Tir" (day + month, no year — for list rows) */
    fun formatShort(date: LocalDate): String {
        val (_, jm, jd) = PersianCalendarConverter.toJalali(date.year, date.monthValue, date.dayOfMonth)
        return "$jd ${monthNames[jm - 1]}"
    }

    /** "21 Tir 1405" (day + month + year) */
    fun formatFull(date: LocalDate): String {
        val (jy, jm, jd) = PersianCalendarConverter.toJalali(date.year, date.monthValue, date.dayOfMonth)
        return "$jd ${monthNames[jm - 1]} $jy"
    }

    /** "Yekshanbe, 21 Tir 1405" (weekday + day + month + year) */
    fun formatFullWithWeekday(date: LocalDate): String {
        val (jy, jm, jd) = PersianCalendarConverter.toJalali(date.year, date.monthValue, date.dayOfMonth)
        val weekday = weekdayNames[date.dayOfWeek].orEmpty()
        return "$weekday, $jd ${monthNames[jm - 1]} $jy"
    }

    /** Bare year, e.g. "1405". */
    fun yearOnly(date: LocalDate): String {
        val (jy, _, _) = PersianCalendarConverter.toJalali(date.year, date.monthValue, date.dayOfMonth)
        return jy.toString()
    }

    /** Month name only, e.g. "Tir". */
    fun monthOnly(date: LocalDate): String {
        val (_, jm, _) = PersianCalendarConverter.toJalali(date.year, date.monthValue, date.dayOfMonth)
        return monthNames[jm - 1]
    }

    /** Month name + 2-digit year, e.g. "Tir 05". */
    fun monthYearShort(date: LocalDate): String {
        val (jy, jm, _) = PersianCalendarConverter.toJalali(date.year, date.monthValue, date.dayOfMonth)
        val shortYear = (jy % 100).toString().padStart(2, '0')
        return "${monthNames[jm - 1]} $shortYear"
    }
}
