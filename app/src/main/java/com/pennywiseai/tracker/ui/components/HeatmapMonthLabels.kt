package com.pennywiseai.tracker.ui.components

import com.pennywiseai.tracker.utils.DateFormatter
import java.time.LocalDate

fun buildHeatmapMonthLabels(
    startDate: LocalDate,
    endDate: LocalDate
): List<Pair<Int, String>> {
    val allMonthStarts = mutableListOf<Pair<Int, String>>()
    var current = startDate
    var lastMonth = -1
    var weekIndex = 0

    while (current <= endDate) {
        val month = DateFormatter.calendarMonthValue(current)
        if (month != lastMonth) {
            allMonthStarts.add(weekIndex to DateFormatter.formatMonth(current))
            lastMonth = month
        }
        current = current.plusWeeks(1)
        weekIndex++
    }

    val filteredLabels = mutableListOf<Pair<Int, String>>()
    for (i in allMonthStarts.indices) {
        val (week, label) = allMonthStarts[i]
        if (i == 0 && allMonthStarts.size > 1) {
            val nextWeek = allMonthStarts[1].first
            if (nextWeek - week < 4) continue
        }
        if (filteredLabels.isEmpty()) {
            filteredLabels.add(week to label)
        } else {
            val lastAddedWeek = filteredLabels.last().first
            if (week - lastAddedWeek >= 4) {
                filteredLabels.add(week to label)
            }
        }
    }
    return filteredLabels
}
