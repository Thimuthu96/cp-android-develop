/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.data

import cloud.orelit.communicationplatform.features.home.impl.calendar.create.EventRecurrence
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class CalendarRecurrenceTest {
    @Test
    fun `an event that does not repeat happens once`() {
        val occurrences = occurrencesIn(aSchedule(), FIRST_DAY, FIRST_DAY.plusDays(30))

        assertThat(occurrences).containsExactly(START)
    }

    @Test
    fun `an event that does not repeat is left out of a window it misses`() {
        val occurrences = occurrencesIn(aSchedule(), FIRST_DAY.plusDays(1), FIRST_DAY.plusDays(30))

        assertThat(occurrences).isEmpty()
    }

    @Test
    fun `a daily event happens on every day of the window`() {
        val occurrences = occurrencesIn(aSchedule(rrule = "FREQ=DAILY"), FIRST_DAY, FIRST_DAY.plusDays(4))

        assertThat(occurrences).containsExactly(
            START,
            START.plusDays(1),
            START.plusDays(2),
            START.plusDays(3),
        ).inOrder()
    }

    @Test
    fun `a weekly event keeps its day of the week`() {
        // 2026-09-24 is a Thursday.
        val rrule = EventRecurrence.Weekly.toRRule(START.toLocalDate())

        val occurrences = occurrencesIn(aSchedule(rrule = rrule), FIRST_DAY, FIRST_DAY.plusDays(21))

        assertThat(occurrences).containsExactly(START, START.plusDays(7), START.plusDays(14)).inOrder()
        assertThat(occurrences.map { it.dayOfWeek }.distinct()).containsExactly(START.dayOfWeek)
    }

    @Test
    fun `an event on every weekday skips the weekend`() {
        val rrule = EventRecurrence.Weekdays.toRRule(START.toLocalDate())

        val occurrences = occurrencesIn(aSchedule(rrule = rrule), FIRST_DAY, FIRST_DAY.plusDays(7))

        // Thursday and Friday, then Monday and Tuesday of the following week.
        assertThat(occurrences).containsExactly(
            START,
            START.plusDays(1),
            START.plusDays(4),
            START.plusDays(5),
            START.plusDays(6),
        ).inOrder()
    }

    @Test
    fun `a monthly event skips the months that are too short`() {
        val start = LocalDateTime.of(2026, 1, 31, 14, 0)
        val schedule = aSchedule(start = start, end = start.plusHours(1), rrule = "FREQ=MONTHLY;BYMONTHDAY=31")

        val occurrences = occurrencesIn(schedule, start.toLocalDate(), LocalDate.of(2026, 6, 1))

        // February, April and June have no 31st.
        assertThat(occurrences.map { it.toLocalDate() }).containsExactly(
            LocalDate.of(2026, 1, 31),
            LocalDate.of(2026, 3, 31),
            LocalDate.of(2026, 5, 31),
        ).inOrder()
    }

    @Test
    fun `a yearly event happens once a year`() {
        val occurrences = occurrencesIn(aSchedule(rrule = "FREQ=YEARLY"), FIRST_DAY, FIRST_DAY.plusYears(2))

        assertThat(occurrences).containsExactly(START, START.plusYears(1)).inOrder()
    }

    @Test
    fun `a window after the first occurrence still reports the series`() {
        val occurrences = occurrencesIn(
            aSchedule(rrule = "FREQ=DAILY"),
            windowStart = FIRST_DAY.plusDays(10),
            windowEnd = FIRST_DAY.plusDays(13),
        )

        assertThat(occurrences).containsExactly(
            START.plusDays(10),
            START.plusDays(11),
            START.plusDays(12),
        ).inOrder()
    }

    @Test
    fun `a rule this app does not write is treated as a one-off event`() {
        // COUNT, UNTIL and INTERVAL would all be expanded wrongly, so the series is not expanded at all.
        val cases = listOf("FREQ=DAILY;COUNT=3", "FREQ=DAILY;UNTIL=20261001T000000Z", "FREQ=DAILY;INTERVAL=2", "FREQ=HOURLY", "nonsense")

        cases.forEach { rrule ->
            val occurrences = occurrencesIn(aSchedule(rrule = rrule), FIRST_DAY, FIRST_DAY.plusDays(10))
            assertThat(occurrences).containsExactly(START)
        }
    }

    @Test
    fun `a weekly rule with no day never repeats`() {
        val occurrences = occurrencesIn(aSchedule(rrule = "FREQ=WEEKLY;BYDAY=XX"), FIRST_DAY, FIRST_DAY.plusDays(30))

        assertThat(occurrences).containsExactly(START)
    }

    @Test
    fun `an event running over midnight is reported on the day it starts`() {
        val start = LocalDateTime.of(2026, 9, 24, 23, 0)
        val schedule = aSchedule(start = start, end = start.plusHours(3))

        // The window covers only the following day, which the event runs into.
        val occurrences = occurrencesIn(schedule, LocalDate.of(2026, 9, 25), LocalDate.of(2026, 9, 26))

        assertThat(occurrences).containsExactly(start)
    }

    private fun occurrencesIn(
        schedule: CalendarSchedule,
        windowStart: LocalDate,
        windowEnd: LocalDate,
    ) = CalendarRecurrence.occurrences(schedule, windowStart, windowEnd)

    private fun aSchedule(
        start: LocalDateTime = START,
        end: LocalDateTime = START.plusHours(1),
        rrule: String? = null,
    ) = CalendarSchedule(
        start = start,
        end = end,
        timeZone = ZoneId.of("Europe/Berlin"),
        isAllDay = false,
        rrule = rrule,
    )

    private companion object {
        /** A Thursday, so the weekday and weekly cases are easy to read. */
        val START: LocalDateTime = LocalDateTime.of(2026, 9, 24, 14, 0)
        val FIRST_DAY: LocalDate = START.toLocalDate()
    }
}
