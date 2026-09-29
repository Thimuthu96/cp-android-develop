/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.data

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * When an event happens, as stored in the room topic of its event room.
 *
 * Times are local wall-clock times in [timeZone], like the `DTSTART` / `DTEND` of an iCalendar entry, so an
 * event keeps the time it was created for even when a participant reads it from another time zone.
 * [end] is exclusive, and for an all-day event both bounds are midnights.
 */
@Immutable
data class CalendarSchedule(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val timeZone: ZoneId,
    val isAllDay: Boolean,
    /** An RFC 5545 recurrence rule, or null when the event happens only once. */
    val rrule: String? = null,
    /** True once the organiser called the whole event off. The room and its chat are kept. */
    val isCancelled: Boolean = false,
    /** The original starts, in [timeZone], of the single occurrences the organiser called off. */
    val cancelledOccurrences: Set<LocalDateTime> = emptySet(),
    /** The occurrences the organiser changed on their own, keyed by their original start in [timeZone]. */
    val overrides: Map<LocalDateTime, OccurrenceOverride> = emptyMap(),
) {
    /** True when this schedule carries anything an older version of the payload cannot express. */
    val hasExceptions: Boolean get() = isCancelled || cancelledOccurrences.isNotEmpty() || overrides.isNotEmpty()
}

/**
 * A single occurrence of a series that the organiser moved or renamed, like a `RECURRENCE-ID` entry of iCalendar.
 *
 * [start] and [end] are wall-clock times in the time zone of the series. A null [title] or [description] keeps the
 * one of the series.
 */
@Immutable
data class OccurrenceOverride(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val title: String? = null,
    val description: String? = null,
)

/** Moves a wall-clock time from the zone [from] to the zone [to], keeping the instant it describes. */
internal fun LocalDateTime.moveZone(from: ZoneId, to: ZoneId): LocalDateTime {
    if (from == to) return this
    return atZone(from).withZoneSameInstant(to).toLocalDateTime()
}
