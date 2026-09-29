/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.data

import cloud.orelit.communicationplatform.features.home.impl.calendar.create.NewEventForm
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDateTime

class FakeUpdateCalendarEventAction(
    private val result: () -> Result<Unit> = { Result.success(Unit) },
    private val notUpdated: List<UserId> = emptyList(),
) : UpdateCalendarEventAction {
    data class SeriesEdit(val roomId: RoomId, val form: NewEventForm, val initialParticipants: List<UserId>)
    data class OccurrenceEdit(val roomId: RoomId, val occurrenceStart: LocalDateTime, val form: NewEventForm)
    data class Cancellation(val roomId: RoomId, val occurrenceStart: LocalDateTime?)

    val seriesEdits = mutableListOf<SeriesEdit>()
    val occurrenceEdits = mutableListOf<OccurrenceEdit>()
    val cancellations = mutableListOf<Cancellation>()

    override suspend fun editSeries(roomId: RoomId, form: NewEventForm, initialParticipants: List<UserId>): Result<EditedCalendarEvent> {
        seriesEdits += SeriesEdit(roomId, form, initialParticipants)
        return result().map { EditedCalendarEvent(notUpdated = notUpdated.toImmutableList()) }
    }

    override suspend fun editOccurrence(roomId: RoomId, occurrenceStart: LocalDateTime, form: NewEventForm): Result<Unit> {
        occurrenceEdits += OccurrenceEdit(roomId, occurrenceStart, form)
        return result()
    }

    override suspend fun cancel(roomId: RoomId, occurrenceStart: LocalDateTime?): Result<Unit> {
        cancellations += Cancellation(roomId, occurrenceStart)
        return result()
    }
}
