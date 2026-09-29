/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.create

import androidx.compose.runtime.Immutable
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * The content of the "new event" form.
 *
 * For an all day event only the dates of [start] and [end] matter, and [end] is inclusive: an event
 * on a single day has the same date for both. The times are kept so switching the option off restores them.
 */
@Immutable
data class NewEventForm(
    val title: String,
    val description: String,
    val isAllDay: Boolean,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val recurrence: EventRecurrence,
    val participants: ImmutableList<MatrixUser>,
) {
    val isTimeRangeValid: Boolean
        get() = if (isAllDay) !end.toLocalDate().isBefore(start.toLocalDate()) else end.isAfter(start)

    val canSave: Boolean
        get() = title.isNotBlank() && isTimeRangeValid

    /** True when the user typed or picked something, to confirm before discarding the form. */
    val isDirty: Boolean
        get() = title.isNotBlank() || description.isNotBlank() || participants.isNotEmpty()

    /** Moves [start], and [end] with it so the duration is kept, like calendar apps do. */
    fun withStart(newStart: LocalDateTime): NewEventForm {
        val duration = Duration.between(start, end).takeIf { !it.isNegative && !it.isZero } ?: DEFAULT_DURATION
        return copy(start = newStart, end = newStart.plus(duration))
    }

    fun withEnd(newEnd: LocalDateTime): NewEventForm = copy(end = newEnd)

    fun withParticipant(user: MatrixUser): NewEventForm {
        return if (participants.any { it.userId == user.userId }) this else copy(participants = (participants + user).toImmutableList())
    }

    fun withoutParticipant(user: MatrixUser): NewEventForm {
        return copy(participants = participants.filterNot { it.userId == user.userId }.toImmutableList())
    }

    companion object {
        val DEFAULT_DURATION: Duration = Duration.ofHours(1)
        private val DEFAULT_START_TIME = LocalTime.of(9, 0)

        /**
         * A form for an event on [date]. Today it starts at the next half hour, so the event is in the future,
         * any other day it starts at 9:00.
         */
        fun initial(date: LocalDate, now: LocalDateTime): NewEventForm {
            val start = if (date == now.toLocalDate()) {
                val nextHalfHour = now.truncatedTo(ChronoUnit.HOURS).plusMinutes(if (now.minute < 30) 30 else 60)
                nextHalfHour
            } else {
                date.atTime(DEFAULT_START_TIME)
            }
            return NewEventForm(
                title = "",
                description = "",
                isAllDay = false,
                start = start,
                end = start.plus(DEFAULT_DURATION),
                recurrence = EventRecurrence.None,
                participants = persistentListOf(),
            )
        }
    }
}
