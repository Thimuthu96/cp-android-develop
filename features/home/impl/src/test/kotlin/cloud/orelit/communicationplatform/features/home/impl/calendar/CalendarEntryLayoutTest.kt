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
import java.time.LocalDateTime

class CalendarEntryLayoutTest {
    private val date = LocalDate.of(2026, 9, 21)

    private fun at(hour: Int, minute: Int = 0, day: LocalDate = date): LocalDateTime = day.atTime(hour, minute)

    private fun entry(
        id: String,
        start: LocalDateTime,
        end: LocalDateTime,
        isAllDay: Boolean = false,
    ) = aCalendarEntry(id = id, start = start, end = end, isAllDay = isAllDay)

    @Test
    fun `no entries gives an empty layout`() {
        assertThat(layoutEntriesForDay(emptyList(), date)).isEmpty()
    }

    @Test
    fun `a single entry takes the whole width at its own time`() {
        val result = layoutEntriesForDay(listOf(entry("a", at(9), at(10, 30))), date)
        assertThat(result).hasSize(1)
        with(result.single()) {
            assertThat(startMinute).isEqualTo(9 * 60)
            assertThat(endMinute).isEqualTo(10 * 60 + 30)
            assertThat(column).isEqualTo(0)
            assertThat(columnCount).isEqualTo(1)
        }
    }

    @Test
    fun `back to back entries do not overlap`() {
        val result = layoutEntriesForDay(
            listOf(entry("a", at(9), at(10)), entry("b", at(10), at(11))),
            date,
        )
        assertThat(result.map { it.columnCount }).containsExactly(1, 1)
        assertThat(result.map { it.column }).containsExactly(0, 0)
    }

    @Test
    fun `overlapping entries are displayed side by side`() {
        val result = layoutEntriesForDay(
            listOf(entry("a", at(9), at(10)), entry("b", at(9, 30), at(10, 30))),
            date,
        ).associateBy { it.entry.id }
        assertThat(result.getValue("a").column).isEqualTo(0)
        assertThat(result.getValue("b").column).isEqualTo(1)
        assertThat(result.values.map { it.columnCount }).containsExactly(2, 2)
    }

    @Test
    fun `a chain of overlaps shares the columns of its cluster and reuses free columns`() {
        // a overlaps b, b overlaps c, but a does not overlap c
        val result = layoutEntriesForDay(
            listOf(
                entry("a", at(9), at(10)),
                entry("b", at(9, 30), at(10, 30)),
                entry("c", at(10), at(11)),
            ),
            date,
        ).associateBy { it.entry.id }
        assertThat(result.getValue("a").column).isEqualTo(0)
        assertThat(result.getValue("b").column).isEqualTo(1)
        assertThat(result.getValue("c").column).isEqualTo(0)
        assertThat(result.values.map { it.columnCount }).containsExactly(2, 2, 2)
    }

    @Test
    fun `separate clusters do not share their column count`() {
        val result = layoutEntriesForDay(
            listOf(
                entry("a", at(9), at(10)),
                entry("b", at(9), at(10)),
                entry("c", at(14), at(15)),
            ),
            date,
        ).associateBy { it.entry.id }
        assertThat(result.getValue("a").columnCount).isEqualTo(2)
        assertThat(result.getValue("b").columnCount).isEqualTo(2)
        assertThat(result.getValue("c").columnCount).isEqualTo(1)
    }

    @Test
    fun `a very short entry keeps a minimum duration`() {
        val result = layoutEntriesForDay(listOf(entry("a", at(10), at(10, 10))), date).single()
        assertThat(result.endMinute - result.startMinute).isEqualTo(MIN_ENTRY_DURATION_MINUTES)
        assertThat(result.startMinute).isEqualTo(10 * 60)
    }

    @Test
    fun `a very short entry at the end of the day stays inside the day`() {
        val result = layoutEntriesForDay(listOf(entry("a", at(23, 50), at(23, 55))), date).single()
        assertThat(result.endMinute).isEqualTo(MINUTES_PER_DAY)
        assertThat(result.endMinute - result.startMinute).isEqualTo(MIN_ENTRY_DURATION_MINUTES)
    }

    @Test
    fun `all day entries are not part of the time grid`() {
        val allDay = entry("a", date.atStartOfDay(), date.plusDays(1).atStartOfDay(), isAllDay = true)
        assertThat(layoutEntriesForDay(listOf(allDay), date)).isEmpty()
    }

    @Test
    fun `entries of other days are ignored`() {
        val tomorrow = date.plusDays(1)
        assertThat(layoutEntriesForDay(listOf(entry("a", at(9, day = tomorrow), at(10, day = tomorrow))), date)).isEmpty()
    }

    @Test
    fun `an entry crossing midnight is clipped on each day`() {
        val overnight = entry("a", at(22), at(2, day = date.plusDays(1)))
        val first = layoutEntriesForDay(listOf(overnight), date).single()
        assertThat(first.startMinute).isEqualTo(22 * 60)
        assertThat(first.endMinute).isEqualTo(MINUTES_PER_DAY)
        val second = layoutEntriesForDay(listOf(overnight), date.plusDays(1)).single()
        assertThat(second.startMinute).isEqualTo(0)
        assertThat(second.endMinute).isEqualTo(2 * 60)
    }

    @Test
    fun `all day row count is the busiest day, capped`() {
        fun allDay(id: String, day: LocalDate) = entry(id, day.atStartOfDay(), day.plusDays(1).atStartOfDay(), isAllDay = true)
        val entries = listOf(
            allDay("a", date),
            allDay("b", date),
            allDay("c", date.plusDays(1)),
        )
        assertThat(allDayRowCount(entries, date, days = 3)).isEqualTo(2)
        assertThat(allDayRowCount(entries, date, days = 3, maxRows = 1)).isEqualTo(1)
        assertThat(allDayRowCount(entries, date.plusDays(5), days = 3)).isEqualTo(0)
        assertThat(allDayRowCount(emptyList(), date, days = 1)).isEqualTo(0)
    }
}
