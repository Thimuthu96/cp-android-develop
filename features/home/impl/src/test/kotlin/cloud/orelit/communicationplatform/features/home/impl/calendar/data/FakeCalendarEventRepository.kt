/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.data

import cloud.orelit.communicationplatform.features.home.impl.calendar.CalendarEntry
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

class FakeCalendarEventRepository(
    initialEntries: ImmutableList<CalendarEntry> = persistentListOf(),
) : CalendarEventRepository {
    val entries = MutableStateFlow(initialEntries)

    /** The arguments of the last call, so a test can check the window the presenter asks for. */
    var lastRequest: Request? = null
        private set

    data class Request(val around: LocalDate, val daysBefore: Long, val daysAfter: Long)

    override fun entriesFlow(
        around: LocalDate,
        daysBefore: Long,
        daysAfter: Long,
    ): Flow<ImmutableList<CalendarEntry>> {
        lastRequest = Request(around, daysBefore, daysAfter)
        return entries
    }
}
