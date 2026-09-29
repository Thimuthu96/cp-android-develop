/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.EventFormState
import cloud.orelit.communicationplatform.features.home.impl.calendar.detail.CalendarEventDetailsState
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.toImmutableList
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/** A fixed "now" (a Monday) so the previews and the screenshots are deterministic. */
internal val A_CALENDAR_NOW: LocalDateTime = LocalDateTime.of(2026, 9, 21, 10, 24)

open class CalendarStatePreviewParam : PreviewParameterProvider<CalendarState> {
    override val values: Sequence<CalendarState>
        get() = sequenceOf(
            aCalendarState(viewMode = CalendarViewMode.Agenda),
            aCalendarState(viewMode = CalendarViewMode.Day),
            aCalendarState(viewMode = CalendarViewMode.ThreeDay),
            aCalendarState(viewMode = CalendarViewMode.Week),
            aCalendarState(viewMode = CalendarViewMode.Agenda, entries = emptyList()),
            aCalendarState(viewMode = CalendarViewMode.Day, entries = emptyList()),
        )
}

open class CalendarEntryPreviewParam : PreviewParameterProvider<CalendarEntry> {
    override val values: Sequence<CalendarEntry>
        get() = sequenceOf(
            aCalendarEntry(),
            aCalendarEntry(location = "Room 3.02", isOnlineMeeting = true, accentIndex = 1),
            aCalendarEntry(title = "A very long title that does not fit on a single line of the card", accentIndex = 2),
            aCalendarEntry(title = "Budget review", location = "Room 3.02", isOnlineMeeting = true, accentIndex = 1, isCancelled = true),
        )
}

internal fun aCalendarEntry(
    id: String = "id",
    title: String = "Design review",
    start: LocalDateTime = A_CALENDAR_NOW.withHour(11).withMinute(0),
    end: LocalDateTime = start.plusHours(1),
    isAllDay: Boolean = false,
    location: String? = null,
    isOnlineMeeting: Boolean = false,
    accentIndex: Int = 0,
    description: String? = null,
    rrule: String? = null,
    room: CalendarEntryRoom? = null,
    isCancelled: Boolean = false,
    occurrenceStart: LocalDateTime? = null,
) = CalendarEntry(
    id = id,
    title = title,
    start = start,
    end = end,
    isAllDay = isAllDay,
    location = location,
    isOnlineMeeting = isOnlineMeeting,
    accentIndex = accentIndex,
    description = description,
    rrule = rrule,
    room = room,
    isCancelled = isCancelled,
    occurrenceStart = occurrenceStart,
)

internal fun aCalendarEntryRoom(
    roomId: RoomId = RoomId("!event:example.org"),
    organizer: UserId? = UserId("@alice:example.org"),
    participants: List<MatrixUser> = listOf(aCalendarUser(), aCalendarUser("@bob:example.org", "Bob")),
    participantCount: Long = participants.size.toLong(),
    hasActiveCall: Boolean = false,
) = CalendarEntryRoom(
    roomId = roomId,
    organizer = organizer,
    participants = participants.toImmutableList(),
    participantCount = participantCount,
    hasActiveCall = hasActiveCall,
    canonicalAlias = null,
)

internal fun aCalendarUser(
    id: String = "@alice:example.org",
    displayName: String? = "Alice",
) = MatrixUser(userId = UserId(id), displayName = displayName)

internal fun aCalendarState(
    selectedDate: LocalDate = A_CALENDAR_NOW.toLocalDate(),
    viewMode: CalendarViewMode = CalendarViewMode.Day,
    now: LocalDateTime = A_CALENDAR_NOW,
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    entries: List<CalendarEntry> = CalendarSampleData.entriesAround(now.toLocalDate()),
    eventForm: EventFormState? = null,
    eventDetails: CalendarEventDetailsState? = null,
    eventSink: (CalendarEvent) -> Unit = {},
) = CalendarState(
    selectedDate = selectedDate,
    viewMode = viewMode,
    now = now,
    firstDayOfWeek = firstDayOfWeek,
    entries = entries.toImmutableList(),
    eventForm = eventForm,
    eventDetails = eventDetails,
    eventSink = eventSink,
)
