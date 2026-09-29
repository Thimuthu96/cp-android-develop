/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.create

import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.usersearch.api.UserSearchResult
import kotlinx.collections.immutable.ImmutableList
import java.time.LocalDateTime

data class EventFormState(
    val form: NewEventForm,
    val saveAction: AsyncAction<Unit>,
    /** Not null while the user is picking the participants. */
    val participantPicker: ParticipantPickerState?,
    val mode: EventFormMode = EventFormMode.Create,
    /** What the form held when it was opened, to tell whether an edit changed anything. Null for a new event. */
    val initialForm: NewEventForm? = null,
    val eventSink: (EventFormEvent) -> Unit,
) {
    val isSaving: Boolean get() = saveAction is AsyncAction.Loading

    /** An edit that changes nothing has nothing to save. */
    val canSave: Boolean get() = form.canSave && !isSaving && (!isEditing || isDirty)

    val isEditing: Boolean get() = mode !is EventFormMode.Create

    /** A single occurrence cannot change how the series repeats, who is invited, or become an all-day event. */
    val isEditingOccurrence: Boolean get() = mode is EventFormMode.EditOccurrence

    /** True when closing the form would lose something the user typed or picked, so it is confirmed first. */
    val isDirty: Boolean get() = initialForm?.let { it != form } ?: form.isDirty
}

/** What saving the form does. */
sealed interface EventFormMode {
    /** Creates a new event. */
    data object Create : EventFormMode

    /** Changes a one-off event, or every occurrence of a series. */
    data class EditSeries(val roomId: RoomId) : EventFormMode

    /**
     * Changes a single occurrence of a series.
     *
     * @param roomId the room of the event.
     * @param occurrenceStart the original start of the occurrence, in the time zone of the series.
     */
    data class EditOccurrence(val roomId: RoomId, val occurrenceStart: LocalDateTime) : EventFormMode
}

data class ParticipantPickerState(
    val query: String,
    val results: ImmutableList<UserSearchResult>,
    val isSearching: Boolean,
)
