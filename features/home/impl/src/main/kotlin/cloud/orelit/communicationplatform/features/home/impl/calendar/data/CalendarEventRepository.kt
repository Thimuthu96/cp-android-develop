/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.data

import cloud.orelit.communicationplatform.features.home.impl.calendar.CalendarEntry
import cloud.orelit.communicationplatform.features.home.impl.calendar.CalendarEntryRoom
import cloud.orelit.communicationplatform.libraries.core.coroutine.CoroutineDispatchers
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.room.CurrentUserMembership
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomInfo
import cloud.orelit.communicationplatform.libraries.matrix.api.roomlist.RoomSummary
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * The calendar entries of the logged in user.
 *
 * The homeserver has no calendar API: an event is a room whose topic carries its schedule, see [CalendarRoomEncoding].
 * Reading the calendar is therefore reading the room list, which the SDK already keeps in sync and stores offline.
 */
interface CalendarEventRepository {
    /**
     * The entries overlapping the window around the given day, refreshed whenever the rooms change.
     * A recurring event contributes one entry per occurrence in the window.
     *
     * @param around the day the window is centred on.
     * @param daysBefore how many days before [around] to report.
     * @param daysAfter how many days after [around] to report.
     */
    fun entriesFlow(
        around: LocalDate,
        daysBefore: Long,
        daysAfter: Long,
    ): Flow<ImmutableList<CalendarEntry>>
}

@ContributesBinding(SessionScope::class)
@Inject
class DefaultCalendarEventRepository(
    private val client: MatrixClient,
    private val coroutineDispatchers: CoroutineDispatchers,
) : CalendarEventRepository {
    override fun entriesFlow(
        around: LocalDate,
        daysBefore: Long,
        daysAfter: Long,
    ): Flow<ImmutableList<CalendarEntry>> {
        val windowStart = around.minusDays(daysBefore)
        // Exclusive, so the last reported day is covered in full.
        val windowEnd = around.plusDays(daysAfter + 1)
        return client.roomListService.allRooms.summaries.map { summaries ->
            summaries
                .flatMap { summary -> summary.toEntries(windowStart, windowEnd) }
                .sortedWith(compareBy({ it.start }, { it.title }))
                .toImmutableList()
        }
            // Every room is parsed on each emission of the room list, which is too much work for the main thread.
            .flowOn(coroutineDispatchers.computation)
    }

    private fun RoomSummary.toEntries(windowStart: LocalDate, windowEnd: LocalDate): List<CalendarEntry> {
        // A space groups rooms, and a room the user left or was only invited to is not on their calendar yet.
        if (info.isSpace || info.currentUserMembership != CurrentUserMembership.JOINED) return emptyList()
        val schedule = CalendarRoomEncoding.decodeSchedule(info.topic) ?: return emptyList()

        val room = info.toCalendarEntryRoom()
        val description = CalendarRoomEncoding.decodeDescription(info.topic)
        val title = info.name?.takeIf { it.isNotBlank() } ?: info.rawName.orEmpty()
        // Derived from the room id so an event keeps its colour across restarts and devices.
        val accentIndex = info.id.value.hashCode()

        return CalendarOccurrences.expand(schedule, windowStart, windowEnd, ZoneId.systemDefault()).map { occurrence ->
            CalendarEntry(
                // Each occurrence needs an id of its own, otherwise a weekly series collapses into a single entry.
                // It is taken from the original date so an open sheet keeps following an occurrence that was moved.
                id = "${info.id.value}@${occurrence.originalLocalDate}",
                title = occurrence.title ?: title,
                start = occurrence.start,
                end = occurrence.end,
                isAllDay = schedule.isAllDay,
                // Every event room can host a call, so every event is an online meeting.
                isOnlineMeeting = true,
                accentIndex = accentIndex,
                description = occurrence.description ?: description,
                rrule = schedule.rrule,
                room = room,
                isCancelled = occurrence.isCancelled,
                occurrenceStart = occurrence.original,
            )
        }
    }

    private fun RoomInfo.toCalendarEntryRoom() = CalendarEntryRoom(
        roomId = id,
        organizer = creators.firstOrNull(),
        participants = heroes,
        participantCount = joinedMembersCount,
        hasActiveCall = hasRoomCall,
        canonicalAlias = canonicalAlias,
    )
}

/**
 * Turns a schedule into the occurrences a device shows, with the organiser's changes applied.
 *
 * Kept apart from the room list so the rules can be tested on a schedule alone.
 */
internal object CalendarOccurrences {
    /**
     * One occurrence, ready to be shown.
     *
     * @param original the start the occurrence has in its series, in the time zone of the series.
     * @param originalLocalDate the date of [original] on this device, which identifies the occurrence in the UI.
     * @param start the start on this device, after any change the organiser made to this occurrence.
     * @param end the exclusive end on this device.
     * @param title the title of this occurrence alone, null to use the one of the series.
     * @param description the description of this occurrence alone, null to use the one of the series.
     * @param isCancelled true when the organiser called this occurrence, or the whole series, off.
     */
    data class Occurrence(
        val original: LocalDateTime,
        val originalLocalDate: LocalDate,
        val start: LocalDateTime,
        val end: LocalDateTime,
        val title: String?,
        val description: String?,
        val isCancelled: Boolean,
    )

    fun expand(
        schedule: CalendarSchedule,
        windowStart: LocalDate,
        windowEnd: LocalDate,
        deviceZone: ZoneId,
    ): List<Occurrence> {
        val duration = Duration.between(schedule.start, schedule.end)
        // The series repeats on the event's own wall clock, so it is expanded there and each occurrence is then
        // moved to this device's zone. The window is widened by a day because that move can cross midnight.
        // An override that moves an occurrence further than that out of the window may be missed, which is accepted.
        return CalendarRecurrence.occurrences(schedule, windowStart.minusDays(1), windowEnd.plusDays(1))
            .map { original ->
                // The change is applied on the wall clock of the series, where its key and its times are written.
                val override = schedule.overrides[original]
                val start = override?.start ?: original
                val end = override?.end ?: original.plus(duration)
                Occurrence(
                    original = original,
                    originalLocalDate = original.toLocalZone(schedule, deviceZone).toLocalDate(),
                    start = start.toLocalZone(schedule, deviceZone),
                    end = end.toLocalZone(schedule, deviceZone),
                    title = override?.title,
                    description = override?.description,
                    isCancelled = schedule.isCancelled || original in schedule.cancelledOccurrences,
                )
            }
            .filter { it.start.toLocalDate() < windowEnd && !it.end.toLocalDate().isBefore(windowStart) }
    }

    /**
     * A time of the series expressed in the device's time zone, so an event created elsewhere lands on the grid at
     * the time it actually happens here. An all-day event is a whole date rather than an instant, so it is left alone.
     */
    private fun LocalDateTime.toLocalZone(schedule: CalendarSchedule, deviceZone: ZoneId): LocalDateTime {
        if (schedule.isAllDay) return this
        return moveZone(schedule.timeZone, deviceZone)
    }
}
