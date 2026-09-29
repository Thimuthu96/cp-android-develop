/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class CalendarEntryTest {
    private val date = LocalDate.of(2026, 9, 21)

    @Test
    fun `a timed entry occurs on its day only`() {
        val entry = aCalendarEntry(start = date.atTime(9, 0), end = date.atTime(10, 0))
        assertThat(entry.occursOn(date)).isTrue()
        assertThat(entry.occursOn(date.minusDays(1))).isFalse()
        assertThat(entry.occursOn(date.plusDays(1))).isFalse()
    }

    @Test
    fun `an entry ending at midnight does not occur on the next day`() {
        val entry = aCalendarEntry(start = date.atTime(22, 0), end = date.plusDays(1).atStartOfDay())
        assertThat(entry.occursOn(date)).isTrue()
        assertThat(entry.occursOn(date.plusDays(1))).isFalse()
    }

    @Test
    fun `an entry crossing midnight occurs on both days`() {
        val entry = aCalendarEntry(start = date.atTime(22, 0), end = date.plusDays(1).atTime(1, 0))
        assertThat(entry.occursOn(date)).isTrue()
        assertThat(entry.occursOn(date.plusDays(1))).isTrue()
    }

    @Test
    fun `an all day entry occurs on each day it covers`() {
        val entry = aCalendarEntry(
            start = date.atStartOfDay(),
            end = date.plusDays(2).atStartOfDay(),
            isAllDay = true,
        )
        assertThat(entry.occursOn(date.minusDays(1))).isFalse()
        assertThat(entry.occursOn(date)).isTrue()
        assertThat(entry.occursOn(date.plusDays(1))).isTrue()
        assertThat(entry.occursOn(date.plusDays(2))).isFalse()
    }

    @Test
    fun `an entry without duration occurs on its day`() {
        val entry = aCalendarEntry(start = date.atTime(9, 0), end = date.atTime(9, 0))
        assertThat(entry.occursOn(date)).isTrue()
        assertThat(entry.occursOn(date.plusDays(1))).isFalse()
    }

    @Test
    fun `the state lists the entries of a day`() {
        val today = aCalendarEntry(id = "today", start = date.atTime(9, 0), end = date.atTime(10, 0))
        val tomorrow = aCalendarEntry(id = "tomorrow", start = date.plusDays(1).atTime(9, 0), end = date.plusDays(1).atTime(10, 0))
        val state = aCalendarState(entries = listOf(today, tomorrow))
        assertThat(state.entriesOn(date)).containsExactly(today)
        assertThat(state.entriesOn(date.plusDays(1))).containsExactly(tomorrow)
        assertThat(state.entriesOn(date.plusDays(2))).isEmpty()
    }
}
