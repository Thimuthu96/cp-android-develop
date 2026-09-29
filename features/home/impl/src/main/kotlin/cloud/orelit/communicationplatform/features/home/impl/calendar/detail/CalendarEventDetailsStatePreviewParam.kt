/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.detail

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.features.home.impl.calendar.CalendarEntry
import cloud.orelit.communicationplatform.features.home.impl.calendar.aCalendarEntry
import cloud.orelit.communicationplatform.features.home.impl.calendar.aCalendarEntryRoom
import cloud.orelit.communicationplatform.features.home.impl.calendar.aCalendarUser
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.toImmutableList

open class CalendarEventDetailsStatePreviewParam : PreviewParameterProvider<CalendarEventDetailsState> {
    override val values: Sequence<CalendarEventDetailsState>
        get() = sequenceOf(
            aCalendarEventDetailsState(),
            // A recurring meeting that has already started, with everything the sheet can show.
            aCalendarEventDetailsState(
                entry = aCalendarEntry(
                    title = "Weekly design review",
                    location = "Room 3.02",
                    isOnlineMeeting = true,
                    description = "Walk through the new calendar screens and agree on what ships this sprint.",
                    rrule = "FREQ=WEEKLY;BYDAY=MO",
                    room = aCalendarEntryRoom(hasActiveCall = true),
                ),
                hasActiveCall = true,
            ),
            // An all-day entry with no room behind it, so calling and chatting are unavailable.
            aCalendarEventDetailsState(
                entry = aCalendarEntry(title = "Team offsite", isAllDay = true, accentIndex = 3, room = null),
                organizer = null,
                participants = emptyList(),
                participantCount = 0,
                meetingLink = null,
            ),
            // The organiser of a one-off event, who can edit it and call it off.
            aCalendarEventDetailsState(canManage = true),
            // A cancelled event: nobody can join or change it any more.
            aCalendarEventDetailsState(
                entry = aCalendarEntry(title = "Budget review", room = aCalendarEntryRoom(), isCancelled = true),
                canManage = true,
            ),
            // The organiser of a recurring event, asked which occurrences to edit.
            aCalendarEventDetailsState(
                entry = aCalendarEntry(title = "Weekly design review", rrule = "FREQ=WEEKLY;BYDAY=MO", room = aCalendarEntryRoom()),
                canManage = true,
                pendingAction = ManageAction.AskScope(ManageAction.Kind.Edit),
            ),
            // The last confirmation before cancelling.
            aCalendarEventDetailsState(
                canManage = true,
                pendingAction = ManageAction.ConfirmCancel(EventScope.Series),
            ),
        )
}

internal fun aCalendarEventDetailsState(
    entry: CalendarEntry = aCalendarEntry(room = aCalendarEntryRoom()),
    organizer: MatrixUser? = aCalendarUser(),
    participants: List<MatrixUser> = listOf(aCalendarUser(), aCalendarUser("@bob:example.org", "Bob")),
    participantCount: Long = participants.size.toLong(),
    hasActiveCall: Boolean = false,
    meetingLink: String? = "https://matrix.to/#/!event:example.org",
    canManage: Boolean = false,
    pendingAction: ManageAction? = null,
    eventSink: (CalendarEventDetailsEvent) -> Unit = {},
) = CalendarEventDetailsState(
    entry = entry,
    organizer = organizer,
    participants = participants.toImmutableList(),
    participantCount = participantCount,
    hasActiveCall = hasActiveCall,
    meetingLink = meetingLink,
    canManage = canManage,
    pendingAction = pendingAction,
    eventSink = eventSink,
)
