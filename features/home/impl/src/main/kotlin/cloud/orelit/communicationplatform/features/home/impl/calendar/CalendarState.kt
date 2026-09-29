/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import cloud.orelit.communicationplatform.features.home.impl.calendar.create.EventFormState
import cloud.orelit.communicationplatform.features.home.impl.calendar.detail.CalendarEventDetailsState
import kotlinx.collections.immutable.ImmutableList
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

data class CalendarState(
    val selectedDate: LocalDate,
    val viewMode: CalendarViewMode,
    /** The current local date and time, refreshed every minute. */
    val now: LocalDateTime,
    val firstDayOfWeek: DayOfWeek,
    /** The occurrences in the window the presenter reads around today, ordered by start time. */
    val entries: ImmutableList<CalendarEntry>,
    /** Not null while the user is creating an event. */
    val eventForm: EventFormState?,
    /** Not null while the details of an event are shown. */
    val eventDetails: CalendarEventDetailsState?,
    val eventSink: (CalendarEvent) -> Unit,
) {
    val today: LocalDate get() = now.toLocalDate()

    fun entriesOn(date: LocalDate): List<CalendarEntry> = entries.filter { it.occursOn(date) }
}
