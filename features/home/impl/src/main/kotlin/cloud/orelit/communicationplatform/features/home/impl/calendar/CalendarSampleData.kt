/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDate
import java.time.LocalTime

/**
 * Entries used by the previews and the screenshot tests.
 *
 * The calendar itself reads real events from `CalendarEventRepository`; these only exist so the views can be
 * rendered without a session, which is why they carry no room and offer no call or chat.
 */
internal object CalendarSampleData {
    fun entriesAround(today: LocalDate): ImmutableList<CalendarEntry> = buildList {
        val weekStart = today.minusDays(today.dayOfWeek.value - 1L)
        fun timed(
            id: String,
            title: String,
            date: LocalDate,
            from: LocalTime,
            to: LocalTime,
            accentIndex: Int,
            location: String? = null,
            isOnlineMeeting: Boolean = false,
        ) = add(
            CalendarEntry(
                id = id,
                title = title,
                start = date.atTime(from),
                end = date.atTime(to),
                location = location,
                isOnlineMeeting = isOnlineMeeting,
                accentIndex = accentIndex,
            )
        )

        // Today
        add(
            CalendarEntry(
                id = "team-offsite",
                title = "Team offsite",
                start = today.atStartOfDay(),
                end = today.plusDays(1).atStartOfDay(),
                isAllDay = true,
                accentIndex = 3,
            )
        )
        timed("standup", "Daily stand-up", today, LocalTime.of(9, 0), LocalTime.of(9, 30), 0, isOnlineMeeting = true)
        timed("design-review", "Design review", today, LocalTime.of(10, 0), LocalTime.of(11, 30), 1, location = "Room 3.02")
        timed("sync", "Backend API sync", today, LocalTime.of(10, 30), LocalTime.of(11, 0), 2, isOnlineMeeting = true)
        timed("lunch", "Lunch with Sam", today, LocalTime.of(12, 30), LocalTime.of(13, 30), 4, location = "Cafeteria")
        timed("planning", "Sprint planning", today, LocalTime.of(14, 0), LocalTime.of(15, 30), 0, isOnlineMeeting = true)
        timed("quick-chat", "Quick chat", today, LocalTime.of(16, 0), LocalTime.of(16, 15), 5)

        // Tomorrow
        timed("one-on-one", "1:1 with manager", today.plusDays(1), LocalTime.of(11, 0), LocalTime.of(11, 45), 2, isOnlineMeeting = true)
        timed("workshop", "Customer workshop", today.plusDays(1), LocalTime.of(14, 0), LocalTime.of(17, 0), 1, location = "Main hall")

        // Rest of the week
        timed("retro", "Sprint retrospective", weekStart.plusDays(4), LocalTime.of(15, 0), LocalTime.of(16, 0), 3, isOnlineMeeting = true)
        timed("release", "Release go / no-go", weekStart.plusDays(3), LocalTime.of(9, 30), LocalTime.of(10, 0), 5, isOnlineMeeting = true)
        add(
            CalendarEntry(
                id = "holiday",
                title = "Public holiday",
                start = weekStart.plusDays(5).atStartOfDay(),
                end = weekStart.plusDays(6).atStartOfDay(),
                isAllDay = true,
                accentIndex = 4,
            )
        )
    }.toImmutableList()
}
