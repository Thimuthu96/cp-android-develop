/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.create

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.features.home.impl.calendar.A_CALENDAR_NOW
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.aMatrixUserList
import cloud.orelit.communicationplatform.libraries.usersearch.api.UserSearchResult
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDateTime

open class EventFormStatePreviewParam : PreviewParameterProvider<EventFormState> {
    override val values: Sequence<EventFormState>
        get() = sequenceOf(
            anEventFormState(),
            anEventFormState(
                form = aNewEventForm(
                    title = "Sprint planning",
                    description = "Plan the next sprint together.",
                    recurrence = EventRecurrence.Weekly,
                    participants = aMatrixUserList().take(3),
                ),
            ),
            anEventFormState(form = aNewEventForm(title = "Team offsite", isAllDay = true)),
            anEventFormState(
                form = aNewEventForm(title = "Backwards", end = A_CALENDAR_NOW.minusHours(2)),
            ),
            anEventFormState(form = aNewEventForm(title = "Sprint planning"), saveAction = AsyncAction.Loading),
            anEventFormState(
                form = aNewEventForm(title = "Sprint planning"),
                saveAction = AsyncAction.Failure(MeetingsBotUnavailableException()),
            ),
            anEventFormState(withParticipantPicker = true),
            // Editing a whole event.
            anEventFormState(
                form = aNewEventForm(title = "Sprint planning", participants = aMatrixUserList().take(2)),
                mode = EventFormMode.EditSeries(RoomId("!event:example.org")),
            ),
            // Editing a single occurrence: how it repeats and who is invited belong to the series.
            anEventFormState(
                form = aNewEventForm(title = "Sprint planning", recurrence = EventRecurrence.Weekly, participants = aMatrixUserList().take(2)),
                mode = EventFormMode.EditOccurrence(RoomId("!event:example.org"), A_CALENDAR_NOW.withHour(11).withMinute(0)),
            ),
        )
}

internal fun aNewEventForm(
    title: String = "",
    description: String = "",
    isAllDay: Boolean = false,
    start: LocalDateTime = A_CALENDAR_NOW.withHour(11).withMinute(0),
    end: LocalDateTime = start.plusHours(1),
    recurrence: EventRecurrence = EventRecurrence.None,
    participants: List<MatrixUser> = emptyList(),
) = NewEventForm(
    title = title,
    description = description,
    isAllDay = isAllDay,
    start = start,
    end = end,
    recurrence = recurrence,
    participants = participants.toImmutableList(),
)

internal fun anEventFormState(
    form: NewEventForm = aNewEventForm(),
    saveAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    withParticipantPicker: Boolean = false,
    mode: EventFormMode = EventFormMode.Create,
    eventSink: (EventFormEvent) -> Unit = {},
) = EventFormState(
    form = form,
    saveAction = saveAction,
    mode = mode,
    initialForm = form.takeIf { mode !is EventFormMode.Create },
    participantPicker = if (withParticipantPicker) {
        ParticipantPickerState(
            query = "al",
            results = aMatrixUserList().take(4).map { UserSearchResult(it) }.toImmutableList(),
            isSearching = false,
        )
    } else {
        null
    },
    eventSink = eventSink,
)
