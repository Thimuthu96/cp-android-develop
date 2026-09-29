/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class CalendarOccurrencesTest {
    private val berlin = ZoneId.of("Europe/Berlin")

    // Thursday 24 September 2026, 14:00 to 15:00, every Thursday.
    private val series = CalendarSchedule(
        start = LocalDateTime.of(2026, 9, 24, 14, 0),
        end = LocalDateTime.of(2026, 9, 24, 15, 0),
        timeZone = berlin,
        isAllDay = false,
        rrule = "FREQ=WEEKLY;BYDAY=TH",
    )

    @Test
    fun `an occurrence without changes keeps the series times and is not cancelled`() {
        val occurrences = expand(series)

        assertThat(occurrences.map { it.start }).containsExactly(
            LocalDateTime.of(2026, 9, 24, 14, 0),
            LocalDateTime.of(2026, 10, 1, 14, 0),
            LocalDateTime.of(2026, 10, 8, 14, 0),
        ).inOrder()
        assertThat(occurrences.none { it.isCancelled }).isTrue()
        assertThat(occurrences.map { it.original }).isEqualTo(occurrences.map { it.start })
    }

    @Test
    fun `an override moves and renames only its own occurrence`() {
        val original = LocalDateTime.of(2026, 10, 1, 14, 0)
        val schedule = series.copy(
            overrides = mapOf(
                original to OccurrenceOverride(
                    start = LocalDateTime.of(2026, 10, 2, 9, 0),
                    end = LocalDateTime.of(2026, 10, 2, 11, 0),
                    title = "Moved",
                    description = "Other room",
                )
            ),
        )

        val occurrences = expand(schedule)

        val moved = occurrences.single { it.original == original }
        assertThat(moved.start).isEqualTo(LocalDateTime.of(2026, 10, 2, 9, 0))
        assertThat(moved.end).isEqualTo(LocalDateTime.of(2026, 10, 2, 11, 0))
        assertThat(moved.title).isEqualTo("Moved")
        assertThat(moved.description).isEqualTo("Other room")
        // Still identified by where it was, so an open sheet follows it.
        assertThat(moved.originalLocalDate).isEqualTo(LocalDate.of(2026, 10, 1))
        assertThat(occurrences.filter { it.original != original }.all { it.title == null && it.start == it.original }).isTrue()
    }

    @Test
    fun `a cancelled occurrence is kept but marked, the others are not`() {
        val cancelled = LocalDateTime.of(2026, 10, 8, 14, 0)

        val occurrences = expand(series.copy(cancelledOccurrences = setOf(cancelled)))

        assertThat(occurrences.filter { it.isCancelled }.map { it.original }).containsExactly(cancelled)
        assertThat(occurrences).hasSize(3)
    }

    @Test
    fun `a cancelled series marks every occurrence`() {
        val occurrences = expand(series.copy(isCancelled = true))

        assertThat(occurrences).hasSize(3)
        assertThat(occurrences.all { it.isCancelled }).isTrue()
    }

    @Test
    fun `an override is applied in the zone of the series before moving to the device zone`() {
        val original = LocalDateTime.of(2026, 10, 1, 14, 0)
        val schedule = series.copy(
            overrides = mapOf(original to OccurrenceOverride(start = LocalDateTime.of(2026, 10, 1, 16, 0), end = LocalDateTime.of(2026, 10, 1, 17, 0))),
        )

        val occurrences = expand(schedule, deviceZone = ZoneId.of("UTC"))

        // Berlin is UTC+2 in October.
        val moved = occurrences.single { it.original == original }
        assertThat(moved.start).isEqualTo(LocalDateTime.of(2026, 10, 1, 14, 0))
        assertThat(moved.end).isEqualTo(LocalDateTime.of(2026, 10, 1, 15, 0))
        // The key stays on the wall clock of the series.
        assertThat(moved.original).isEqualTo(original)
    }

    private fun expand(schedule: CalendarSchedule, deviceZone: ZoneId = berlin) = CalendarOccurrences.expand(
        schedule = schedule,
        windowStart = LocalDate.of(2026, 9, 21),
        windowEnd = LocalDate.of(2026, 10, 12),
        deviceZone = deviceZone,
    )
}
