/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import androidx.compose.runtime.Immutable
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * UI model of a calendar item.
 *
 * Times are local wall-clock times. [end] is exclusive, so an all-day entry on a single day
 * ends at the next day's midnight, like Outlook does.
 */
@Immutable
data class CalendarEntry(
    val id: String,
    val title: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val isAllDay: Boolean = false,
    val location: String? = null,
    val isOnlineMeeting: Boolean = false,
    /** Index in the decorative color palette, wraps around. */
    val accentIndex: Int = 0,
    /** What the organiser wrote about the event, shown in its details. */
    val description: String? = null,
    /** The RFC 5545 rule of the series this entry is an occurrence of, null for a one-off event. */
    val rrule: String? = null,
    /** The room backing this event, null for an entry that has none, such as the sample data. */
    val room: CalendarEntryRoom? = null,
    /** True when the organiser called this occurrence, or the whole event, off. */
    val isCancelled: Boolean = false,
    /**
     * The start this occurrence has in its series, in the time zone of the series and before any change the organiser
     * made to it. It is what identifies the occurrence when it is edited or cancelled on its own.
     */
    val occurrenceStart: LocalDateTime? = null,
) {
    /** True if this entry covers at least a part of [date]. */
    fun occursOn(date: LocalDate): Boolean {
        val dayStart = date.atStartOfDay()
        val dayEnd = dayStart.plusDays(1)
        return start < dayEnd && (end > dayStart || start == end && start >= dayStart)
    }
}

/**
 * The Matrix room an event is held in: the one its participants are invited to, chat in, and call from.
 */
@Immutable
data class CalendarEntryRoom(
    val roomId: RoomId,
    /** Who created the event, which is whoever created its room. */
    val organizer: UserId?,
    /** The participants the room list knows about, which may be fewer than [participantCount]. */
    val participants: ImmutableList<MatrixUser>,
    val participantCount: Long,
    /** True while a call is running in the room, so the details can say the meeting has started. */
    val hasActiveCall: Boolean,
    val canonicalAlias: RoomAlias?,
)
