/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Locale and 12/24 hour setting aware formatters for the calendar. */
@Stable
internal class CalendarFormatters(
    private val locale: Locale,
    private val is24Hour: Boolean,
) {
    private fun formatter(skeleton: String): DateTimeFormatter {
        return DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, skeleton), locale)
    }

    private val monthFormatter by lazy { formatter("MMMM") }
    private val monthYearFormatter by lazy { formatter("MMMMy") }
    private val weekdayShortFormatter by lazy { formatter("EEE") }
    private val weekdayNarrowFormatter by lazy { formatter("EEEEE") }
    private val weekdayLongFormatter by lazy { formatter("EEEE") }
    private val dayOfMonthFormatter by lazy { formatter("d") }
    private val monthDayFormatter by lazy { formatter("MMMd") }
    private val dateFormatter by lazy { formatter("EEEMMMd") }
    private val longDateFormatter by lazy { formatter("EEEEMMMMd") }
    private val timeFormatter by lazy { formatter(if (is24Hour) "Hm" else "hma") }
    private val hourFormatter by lazy { formatter(if (is24Hour) "Hm" else "ha") }

    /** The month name, with the year only when it is not the current one. */
    fun monthTitle(date: LocalDate, today: LocalDate): String {
        return if (date.year == today.year) monthFormatter.format(date) else monthYearFormatter.format(date)
    }

    fun weekdayShort(date: LocalDate): String = weekdayShortFormatter.format(date)
    fun weekdayNarrow(date: LocalDate): String = weekdayNarrowFormatter.format(date)
    fun weekdayLong(date: LocalDate): String = weekdayLongFormatter.format(date)
    fun dayOfMonth(date: LocalDate): String = dayOfMonthFormatter.format(date)
    fun monthDay(date: LocalDate): String = monthDayFormatter.format(date)

    /** A short date with its day of the week, for instance `Mon, Sep 21`. */
    fun date(date: LocalDate): String = dateFormatter.format(date)

    /** A spelled out date, for instance `Monday, September 21`, for the details of an event. */
    fun longDate(date: LocalDate): String = longDateFormatter.format(date)

    fun time(time: LocalTime): String = timeFormatter.format(time)
    fun hour(hour: Int): String = hourFormatter.format(LocalTime.of(hour, 0))

    fun timeRange(entry: CalendarEntry): String {
        return "${time(entry.start.toLocalTime())} – ${time(entry.end.toLocalTime())}"
    }

    /**
     * When the event happens, spelled out over one or two lines worth of text, the way the details of an event
     * read in Outlook: the day, then the times, or the range of days for one that lasts longer.
     *
     * @param allDayLabel what to show instead of the times of an all-day event.
     */
    fun detailWhen(entry: CalendarEntry, allDayLabel: String): String {
        val startDate = entry.start.toLocalDate()
        // The end is exclusive, so a day-long entry ends at the next midnight and its last day is the one before.
        val lastDate = if (entry.isAllDay) entry.end.toLocalDate().minusDays(1) else entry.end.toLocalDate()
        return when {
            entry.isAllDay && startDate == lastDate -> "${longDate(startDate)} · $allDayLabel"
            entry.isAllDay -> "${longDate(startDate)} – ${longDate(lastDate)}"
            startDate == lastDate -> "${longDate(startDate)} · ${timeRange(entry)}"
            else -> "${longDate(startDate)} ${time(entry.start.toLocalTime())} – ${longDate(lastDate)} ${time(entry.end.toLocalTime())}"
        }
    }
}

@Composable
internal fun rememberCalendarFormatters(): CalendarFormatters {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val is24Hour = DateFormat.is24HourFormat(context)
    return remember(locale, is24Hour) { CalendarFormatters(locale, is24Hour) }
}
