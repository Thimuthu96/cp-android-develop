/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.data

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Expands a recurring schedule into the occurrences that fall inside a window.
 *
 * Only the rules this app writes are understood, see `EventRecurrence.toRRule`: `FREQ` with an optional `BYDAY`
 * or `BYMONTHDAY`, no `COUNT`, no `UNTIL` and no `INTERVAL`. Anything else is treated as a one-off event, which
 * shows the series at least at its first occurrence rather than hiding it.
 */
internal object CalendarRecurrence {
    /** Series are unbounded, so a window far in the future must not expand into an unbounded list. */
    private const val MAX_OCCURRENCES = 750

    /**
     * The start times of the occurrences overlapping `[windowStart, windowEnd)`, the first one included.
     *
     * @param schedule the series, whose [CalendarSchedule.start] is its first occurrence.
     * @param windowStart the first date to report occurrences for.
     * @param windowEnd the day after the last date to report occurrences for.
     */
    fun occurrences(
        schedule: CalendarSchedule,
        windowStart: LocalDate,
        windowEnd: LocalDate,
    ): List<LocalDateTime> {
        val rule = parse(schedule.rrule)
        val duration = Duration.between(schedule.start, schedule.end)
        if (rule == null) {
            // A one-off event: it is in the window when it overlaps it at all.
            return if (overlaps(schedule.start, duration, windowStart, windowEnd)) listOf(schedule.start) else emptyList()
        }

        val result = mutableListOf<LocalDateTime>()
        var occurrence = schedule.start
        var guard = 0
        // Occurrences are generated in order, so the walk stops as soon as it passes the window.
        while (occurrence.toLocalDate() < windowEnd && guard++ < MAX_OCCURRENCES) {
            if (overlaps(occurrence, duration, windowStart, windowEnd)) {
                result += occurrence
            }
            occurrence = rule.next(occurrence) ?: break
        }
        return result
    }

    /**
     * True when [rrule] is a rule this app expands into its occurrences. Any other rule is shown as a one-off event,
     * so it has no occurrence that could be edited or cancelled on its own.
     */
    fun isSupported(rrule: String?): Boolean = parse(rrule) != null

    private fun overlaps(
        start: LocalDateTime,
        duration: Duration,
        windowStart: LocalDate,
        windowEnd: LocalDate,
    ): Boolean {
        val end = start.plus(duration)
        return start < windowEnd.atStartOfDay() && (end > windowStart.atStartOfDay() || start == end && start >= windowStart.atStartOfDay())
    }

    private sealed interface Rule {
        fun next(from: LocalDateTime): LocalDateTime?

        data object Daily : Rule {
            override fun next(from: LocalDateTime) = from.plusDays(1)
        }

        /** Weekly on the given days, which is also how "every weekday" is expressed. */
        data class WeeklyOn(val days: Set<DayOfWeek>) : Rule {
            override fun next(from: LocalDateTime): LocalDateTime? {
                if (days.isEmpty()) return null
                // At most 7 steps to land on the next day of the series.
                var candidate = from.plusDays(1)
                repeat(7) {
                    if (candidate.dayOfWeek in days) return candidate
                    candidate = candidate.plusDays(1)
                }
                return null
            }
        }

        /**
         * Monthly on [dayOfMonth]. A month too short for it has no occurrence, like RFC 5545 says,
         * so the 31st repeats only in the months that have one.
         */
        data class MonthlyOn(val dayOfMonth: Int) : Rule {
            override fun next(from: LocalDateTime): LocalDateTime? {
                var month = from.toLocalDate().withDayOfMonth(1)
                repeat(MAX_SKIPPED_MONTHS) {
                    month = month.plusMonths(1)
                    if (dayOfMonth <= month.lengthOfMonth()) {
                        return month.withDayOfMonth(dayOfMonth).atTime(from.toLocalTime())
                    }
                }
                return null
            }

            private companion object {
                // February is the only month that can be skipped, so one year of lookahead is always enough.
                const val MAX_SKIPPED_MONTHS = 12
            }
        }

        data object Yearly : Rule {
            // A 29 February series falls back to 28 February, which is what plusYears does.
            override fun next(from: LocalDateTime) = from.plusYears(1)
        }
    }

    private fun parse(rrule: String?): Rule? {
        if (rrule.isNullOrBlank()) return null
        val parts = rrule.split(';')
            .mapNotNull { part ->
                val key = part.substringBefore('=', missingDelimiterValue = "")
                val value = part.substringAfter('=', missingDelimiterValue = "")
                if (key.isEmpty() || value.isEmpty()) null else key.uppercase() to value
            }
            .toMap()
        // Rules with a bound or a step are not written by this app and would be expanded wrongly.
        if (parts.keys.any { it == "COUNT" || it == "UNTIL" || it == "INTERVAL" }) return null
        return when (parts["FREQ"]?.uppercase()) {
            "DAILY" -> Rule.Daily
            "WEEKLY" -> Rule.WeeklyOn(parts["BYDAY"].orEmpty().toDaysOfWeek())
            "MONTHLY" -> parts["BYMONTHDAY"]?.toIntOrNull()?.takeIf { it in 1..31 }?.let { Rule.MonthlyOn(it) }
            "YEARLY" -> Rule.Yearly
            else -> null
        }
    }

    private fun String.toDaysOfWeek(): Set<DayOfWeek> = split(',')
        .mapNotNull { token ->
            when (token.trim().uppercase()) {
                "MO" -> DayOfWeek.MONDAY
                "TU" -> DayOfWeek.TUESDAY
                "WE" -> DayOfWeek.WEDNESDAY
                "TH" -> DayOfWeek.THURSDAY
                "FR" -> DayOfWeek.FRIDAY
                "SA" -> DayOfWeek.SATURDAY
                "SU" -> DayOfWeek.SUNDAY
                else -> null
            }
        }
        .toSet()
}
