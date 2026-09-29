/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import java.time.LocalDate
import java.time.LocalDateTime

sealed interface CalendarEvent {
    data class SelectDate(val date: LocalDate) : CalendarEvent
    data class SelectViewMode(val mode: CalendarViewMode) : CalendarEvent
    data object GoToToday : CalendarEvent
    data object NewEvent : CalendarEvent

    /** Creates an event starting at the slot the user tapped on the grid. */
    data class NewEventAt(val start: LocalDateTime) : CalendarEvent

    /** Shows the details of the entry the user tapped. */
    data class OpenEntry(val entry: CalendarEntry) : CalendarEvent
}
