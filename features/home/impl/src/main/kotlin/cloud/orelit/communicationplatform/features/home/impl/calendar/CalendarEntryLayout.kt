/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import java.time.Duration
import java.time.LocalDate

/** Entries shorter than this are drawn with this duration so they stay readable and tappable. */
internal const val MIN_ENTRY_DURATION_MINUTES = 30

/**
 * Lays out the timed (non all-day) entries of [date] so overlapping entries are displayed side by side.
 *
 * Entries are grouped in clusters of transitively overlapping entries; inside a cluster each
 * entry is given the first free column, and every entry of the cluster shares the same column count.
 */
internal fun layoutEntriesForDay(
    entries: List<CalendarEntry>,
    date: LocalDate,
    minDurationMinutes: Int = MIN_ENTRY_DURATION_MINUTES,
): List<PositionedEntry> {
    val dayStart = date.atStartOfDay()
    val slots = entries
        .filter { !it.isAllDay && it.occursOn(date) }
        .map { entry ->
            val start = Duration.between(dayStart, entry.start).toMinutes().toInt().coerceIn(0, MINUTES_PER_DAY)
            val end = Duration.between(dayStart, entry.end).toMinutes().toInt().coerceIn(0, MINUTES_PER_DAY)
            val visibleEnd = maxOf(end, start + minDurationMinutes).coerceAtMost(MINUTES_PER_DAY)
            val visibleStart = minOf(start, visibleEnd - minDurationMinutes).coerceAtLeast(0)
            Slot(entry, visibleStart, visibleEnd)
        }
        .sortedWith(compareBy<Slot> { it.start }.thenByDescending { it.end })

    val result = mutableListOf<PositionedEntry>()
    var cluster = mutableListOf<Slot>()
    var clusterEnd = Int.MIN_VALUE

    fun flushCluster() {
        if (cluster.isEmpty()) return
        val columnEnds = mutableListOf<Int>()
        val columns = cluster.map { slot ->
            val free = columnEnds.indexOfFirst { it <= slot.start }
            if (free >= 0) {
                columnEnds[free] = slot.end
                free
            } else {
                columnEnds.add(slot.end)
                columnEnds.lastIndex
            }
        }
        cluster.forEachIndexed { index, slot ->
            result += PositionedEntry(slot.entry, slot.start, slot.end, columns[index], columnEnds.size)
        }
        cluster = mutableListOf()
    }

    for (slot in slots) {
        if (slot.start >= clusterEnd) {
            flushCluster()
            clusterEnd = slot.end
        } else {
            clusterEnd = maxOf(clusterEnd, slot.end)
        }
        cluster += slot
    }
    flushCluster()
    return result
}

/** Number of stacked all-day rows needed to show all the all-day entries of the [days] days from [start]. */
internal fun allDayRowCount(
    entries: List<CalendarEntry>,
    start: LocalDate,
    days: Int,
    maxRows: Int = MAX_ALL_DAY_ROWS,
): Int {
    return (0 until days).maxOf { offset ->
        val date = start.plusDays(offset.toLong())
        entries.count { it.isAllDay && it.occursOn(date) }
    }.coerceAtMost(maxRows)
}

internal const val MAX_ALL_DAY_ROWS = 3

private data class Slot(
    val entry: CalendarEntry,
    val start: Int,
    val end: Int,
)
