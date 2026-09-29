/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.create

import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import java.time.LocalDateTime

sealed interface EventFormEvent {
    data class SetTitle(val title: String) : EventFormEvent
    data class SetDescription(val description: String) : EventFormEvent
    data class SetAllDay(val isAllDay: Boolean) : EventFormEvent
    data class SetStart(val start: LocalDateTime) : EventFormEvent
    data class SetEnd(val end: LocalDateTime) : EventFormEvent
    data class SetRecurrence(val recurrence: EventRecurrence) : EventFormEvent
    data object OpenParticipantPicker : EventFormEvent
    data object CloseParticipantPicker : EventFormEvent
    data class SetParticipantQuery(val query: String) : EventFormEvent

    /** Adds the user to the participants, or removes it if it already is one. */
    data class ToggleParticipant(val user: MatrixUser) : EventFormEvent
    data class RemoveParticipant(val user: MatrixUser) : EventFormEvent
    data object Save : EventFormEvent
    data object ClearSaveError : EventFormEvent

    /** Closes the form and drops its content. */
    data object Dismiss : EventFormEvent
}
