/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.data

import cloud.orelit.communicationplatform.features.home.impl.calendar.create.EventRecurrence
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.NewEventForm
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomInfo
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMember
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMembersState
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Changes or calls off an event the user organised.
 *
 * Everything is stored in the topic of the event's room, see [CalendarRoomEncoding], so every change reads the
 * room's current topic right before writing it back with only its own change applied. That keeps whatever was stored
 * before the change started, but it is not a transaction: when two devices write at the same moment, the last write
 * wins and the other change is lost. The room and its chat are never removed, a cancelled event is only marked as such.
 */
interface UpdateCalendarEventAction {
    /**
     * Replaces the whole event, or series, with the [form], and invites or removes people to match it.
     *
     * @param roomId the room of the event.
     * @param form the event as the organiser saved it.
     * @param initialParticipants the participants the form was opened with. Only the ones among them that are no
     * longer in [form] are removed, so nobody who joined or was invited meanwhile is removed by mistake.
     */
    suspend fun editSeries(roomId: RoomId, form: NewEventForm, initialParticipants: List<UserId>): Result<EditedCalendarEvent>

    /**
     * Changes a single occurrence of a series, leaving the others as they are.
     *
     * @param roomId the room of the event.
     * @param occurrenceStart the original start of the occurrence, in the time zone of the series.
     * @param form the new title, times and description of the occurrence.
     */
    suspend fun editOccurrence(roomId: RoomId, occurrenceStart: LocalDateTime, form: NewEventForm): Result<Unit>

    /**
     * Calls the event off, for good.
     *
     * @param roomId the room of the event.
     * @param occurrenceStart the original start of the single occurrence to call off, or null for the whole event.
     */
    suspend fun cancel(roomId: RoomId, occurrenceStart: LocalDateTime?): Result<Unit>
}

/**
 * The outcome of editing a whole event.
 *
 * @param notUpdated the people who could not be invited or removed. The event is saved without that change, so the
 * organiser can sort them out from its chat.
 */
data class EditedCalendarEvent(
    val notUpdated: ImmutableList<UserId> = persistentListOf(),
)

/** The room does not hold an event this version understands, so it cannot be changed without losing data. */
class NotACalendarEventException : IllegalStateException("The room does not hold a calendar event")

/** How long to wait for the member list of an event room before treating it as unknown. */
private const val MEMBERS_TIMEOUT_MILLIS = 10_000L

/**
 * The members of the room, loaded fresh, or null when they could not be loaded in time.
 *
 * Only a complete list is returned: a pending or failed one may miss people, which must not be mistaken for them
 * having left.
 */
internal suspend fun JoinedRoom.loadMembersOrNull(): List<RoomMember>? {
    updateMembers()
    val state = withTimeoutOrNull(MEMBERS_TIMEOUT_MILLIS) {
        membersStateFlow.first { it is RoomMembersState.Ready || it is RoomMembersState.Error }
    }
    return (state as? RoomMembersState.Ready)?.roomMembers
}

/** The people of an event as its organiser sees them: everyone invited or joined, but not themselves or services. */
internal fun List<RoomMember>.eventParticipants(organizer: UserId): List<RoomMember> =
    filter { it.membership.isActive() && !it.isServiceMember && it.userId != organizer }

@ContributesBinding(SessionScope::class)
@Inject
class DefaultUpdateCalendarEventAction(
    private val client: MatrixClient,
) : UpdateCalendarEventAction {
    override suspend fun editSeries(roomId: RoomId, form: NewEventForm, initialParticipants: List<UserId>): Result<EditedCalendarEvent> =
        editSeries(roomId, form, initialParticipants, ZoneId.systemDefault())

    override suspend fun editOccurrence(roomId: RoomId, occurrenceStart: LocalDateTime, form: NewEventForm): Result<Unit> =
        editOccurrence(roomId, occurrenceStart, form, ZoneId.systemDefault())

    override suspend fun cancel(roomId: RoomId, occurrenceStart: LocalDateTime?): Result<Unit> = withEventRoom(roomId) { room, current, description ->
        val cancelled = if (occurrenceStart == null) {
            current.copy(isCancelled = true)
        } else {
            current.copy(cancelledOccurrences = current.cancelledOccurrences + occurrenceStart)
        }
        room.setTopic(CalendarRoomEncoding.encode(description = description, schedule = cancelled))
            .onFailure { Timber.e(it, "Could not cancel the event in ${room.roomId}") }
    }

    internal suspend fun editSeries(
        roomId: RoomId,
        form: NewEventForm,
        initialParticipants: List<UserId>,
        deviceZone: ZoneId,
    ): Result<EditedCalendarEvent> = withEventRoom(roomId) { room, current, _ ->
        // The event keeps its own time zone, so a time the user did not touch stays exactly what it was.
        val edited = form.toSchedule(zoneId = current.timeZone, deviceZone = deviceZone)
            .let { it.copy(rrule = keptRRule(current, form, it.rrule)) }
        // The exceptions are keyed on the occurrences of the series. Once those move they match nothing any more,
        // so they are dropped, like Teams does.
        val keepsExceptions = edited.start == current.start && edited.rrule == current.rrule
        val schedule = edited.copy(
            isCancelled = current.isCancelled,
            cancelledOccurrences = if (keepsExceptions) current.cancelledOccurrences else emptySet(),
            overrides = if (keepsExceptions) current.overrides else emptyMap(),
        )
        room.setTopic(CalendarRoomEncoding.encode(description = form.description, schedule = schedule))
            .onFailure { Timber.e(it, "Could not update the schedule of the event in ${room.roomId}") }
            .getOrElse { return@withEventRoom Result.failure(it) }
        val title = form.title.trim()
        // The schedule is saved by now, so the participants are updated even when the rename fails.
        val renamed = if (title != room.info().currentTitle()) {
            room.setName(title).onFailure { Timber.e(it, "Could not rename the event in ${room.roomId}") }
        } else {
            Result.success(Unit)
        }
        val notUpdated = syncParticipants(room, initial = initialParticipants, wanted = form.participants.map { it.userId })
        renamed.map { EditedCalendarEvent(notUpdated = notUpdated) }
    }

    internal suspend fun editOccurrence(
        roomId: RoomId,
        occurrenceStart: LocalDateTime,
        form: NewEventForm,
        deviceZone: ZoneId,
    ): Result<Unit> = withEventRoom(roomId) { room, current, description ->
        // Only the times are taken from this: a single occurrence cannot change how the series repeats.
        val moved = form.toSchedule(zoneId = current.timeZone, deviceZone = deviceZone)
        val title = form.title.trim()
        val newDescription = form.description.trim()
        val override = OccurrenceOverride(
            start = moved.start,
            end = moved.end,
            // Only what differs from the series is stored, so a later change of the series still reaches it.
            title = title.takeIf { it != room.info().currentTitle() },
            description = newDescription.takeIf { it != description },
        )
        val schedule = current.copy(overrides = current.overrides + (occurrenceStart to override))
        room.setTopic(CalendarRoomEncoding.encode(description = description, schedule = schedule))
            .onFailure { Timber.e(it, "Could not update an occurrence of the event in ${room.roomId}") }
    }

    /**
     * The rule to store for an edited series.
     *
     * When the organiser left the Repeat option as it was, a rule this app did not write, for instance one with an
     * interval or an end, is kept word for word rather than replaced by the closest one this app can write. A rule
     * this app writes is written again from the new start, which only changes it when the start moved to another day.
     */
    private fun keptRRule(current: CalendarSchedule, form: NewEventForm, written: String?): String? {
        val storedRule = current.rrule ?: return written
        val unchanged = form.recurrence == EventRecurrence.fromRRule(storedRule)
        val isOwnRule = storedRule == EventRecurrence.fromRRule(storedRule).toRRule(current.start.toLocalDate())
        return if (unchanged && !isOwnRule) storedRule else written
    }

    /**
     * Runs [block] with the room of the event, its current schedule and its current description, read right before
     * the change so it is applied on top of whatever the topic holds now.
     */
    private suspend fun <T> withEventRoom(
        roomId: RoomId,
        block: suspend (room: JoinedRoom, current: CalendarSchedule, description: String) -> Result<T>,
    ): Result<T> {
        val room = client.getJoinedRoom(roomId)
            ?: return Result.failure(IllegalStateException("The room of the event is not available"))
        return room.use {
            val topic = room.info().topic
            val current = CalendarRoomEncoding.decodeSchedule(topic)
            if (current == null) {
                Timber.w("The room $roomId does not hold an event that can be changed")
                Result.failure(NotACalendarEventException())
            } else {
                block(room, current, CalendarRoomEncoding.decodeDescription(topic).orEmpty())
            }
        }
    }

    /**
     * Invites the people added to the form and removes the ones taken out of it, one by one, and reports the ones
     * that could not be updated.
     *
     * The event has been saved by then, so a failure here does not fail the edit, which is the same policy as when
     * the event is created. Only people the form was opened with and that the organiser took out are removed, and
     * only when the member list is known for sure: a partial list, or someone who joined meanwhile, must never lead
     * to removing people the organiser did not take out.
     */
    private suspend fun syncParticipants(room: JoinedRoom, initial: List<UserId>, wanted: List<UserId>): ImmutableList<UserId> {
        val members = room.loadMembersOrNull()
        if (members == null) {
            Timber.w("The members of ${room.roomId} are not known, only inviting the participants")
        }
        val current = members.orEmpty().eventParticipants(client.sessionId).map { it.userId }.toSet()
        val wantedSet = wanted.toSet()
        val toInvite = wanted.filterNot { it in current || it == client.sessionId }
        val toRemove = if (members == null) {
            emptyList()
        } else {
            initial.filter { it !in wantedSet && it in current && it != client.sessionId }
        }
        val notInvited = toInvite.filter { userId ->
            room.inviteUserById(userId).onFailure { Timber.w(it, "Could not invite $userId to the event") }.isFailure
        }
        val notRemoved = toRemove.filter { userId ->
            room.kickUser(userId).onFailure { Timber.w(it, "Could not remove $userId from the event") }.isFailure
        }
        return (notInvited + notRemoved).toImmutableList()
    }
}

private fun RoomInfo.currentTitle(): String =
    name?.takeIf { it.isNotBlank() } ?: rawName.orEmpty()
