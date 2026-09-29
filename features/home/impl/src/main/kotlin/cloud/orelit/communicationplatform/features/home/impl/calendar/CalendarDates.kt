/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/** Virtually infinite pager: the page count is large and the initial page is in the middle. */
internal const val CALENDAR_PAGE_COUNT = 20_000
internal const val CALENDAR_INITIAL_PAGE = CALENDAR_PAGE_COUNT / 2

internal const val MINUTES_PER_DAY = 24 * 60

internal fun LocalDate.startOfWeek(firstDayOfWeek: DayOfWeek): LocalDate =
    with(TemporalAdjusters.previousOrSame(firstDayOfWeek))

/** The first date displayed by the pager page [page], where [anchor] is the first date of the initial page. */
internal fun pageToStartDate(page: Int, anchor: LocalDate, step: Int): LocalDate =
    anchor.plusDays((page - CALENDAR_INITIAL_PAGE).toLong() * step)

/** The pager page that displays [date]. */
internal fun dateToPage(date: LocalDate, anchor: LocalDate, step: Int): Int {
    val days = ChronoUnit.DAYS.between(anchor, date)
    return (CALENDAR_INITIAL_PAGE + Math.floorDiv(days, step.toLong())).toInt()
        .coerceIn(0, CALENDAR_PAGE_COUNT - 1)
}

/**
 * The minute of the day at which the "now" indicator must be drawn in the column of [date],
 * or null if [date] is not today.
 */
internal fun nowIndicatorMinute(now: LocalDateTime, date: LocalDate): Int? {
    return if (now.toLocalDate() == date) now.hour * 60 + now.minute else null
}
