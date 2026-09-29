/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.data

import cloud.orelit.communicationplatform.appconfig.MeetingsConfig
import cloud.orelit.communicationplatform.features.enterprise.api.SessionEnterpriseService
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.CreateMeetingAction
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.NewEventForm
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.createroom.CreateRoomParameters
import cloud.orelit.communicationplatform.libraries.matrix.api.createroom.RoomPreset
import cloud.orelit.communicationplatform.libraries.matrix.api.roomdirectory.RoomVisibility
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber
import java.time.ZoneId

/**
 * Creates the event the user filled in the form.
 *
 * An event is a private Matrix room its participants are invited to: it carries the schedule in its topic, doubles
 * as the chat of the event, and is what the meeting call is placed in. Nothing but a homeserver is needed.
 */
interface CreateCalendarEventAction {
    suspend fun execute(form: NewEventForm): Result<CreatedCalendarEvent>
}

/**
 * The outcome of creating an event.
 *
 * @param roomId the room holding the event, or null when the event was handed to the meetings bot, which creates
 * its room a few moments later and invites the user to it.
 * @param notInvited the participants the homeserver refused to invite. The event exists without them, so the
 * organiser can invite them from its chat once the reason is sorted out.
 */
data class CreatedCalendarEvent(
    val roomId: RoomId?,
    val notInvited: ImmutableList<UserId> = persistentListOf(),
)

@ContributesBinding(SessionScope::class)
@Inject
class DefaultCreateCalendarEventAction(
    private val client: MatrixClient,
    private val sessionEnterpriseService: SessionEnterpriseService,
    private val createMeetingAction: CreateMeetingAction,
) : CreateCalendarEventAction {
    override suspend fun execute(form: NewEventForm): Result<CreatedCalendarEvent> = execute(form, ZoneId.systemDefault())

    internal suspend fun execute(form: NewEventForm, zoneId: ZoneId): Result<CreatedCalendarEvent> {
        // A deployment that runs the meetings bot keeps using it, so its rooms stay the single source of truth.
        if (MeetingsConfig.BOT_USER_ID != null) {
            return createMeetingAction.execute(form).map { CreatedCalendarEvent(roomId = null) }
        }

        val schedule = form.toSchedule(zoneId)
        val parameters = CreateRoomParameters(
            name = form.title.trim(),
            topic = CalendarRoomEncoding.encode(description = form.description, schedule = schedule),
            isEncrypted = !sessionEnterpriseService.isEncryptionDisabledByHomeserver(),
            isDirect = false,
            visibility = RoomVisibility.Private,
            preset = RoomPreset.PRIVATE_CHAT,
            // Deliberately created empty: see inviteParticipants.
            invite = null,
        )
        val roomId = client.createRoom(parameters)
            .onFailure { Timber.e(it, "Could not create the room of the event") }
            .getOrElse { return Result.failure(it) }

        return Result.success(
            CreatedCalendarEvent(
                roomId = roomId,
                notInvited = inviteParticipants(roomId, form.participants.map { it.userId }),
            )
        )
    }

    /**
     * Invites the participants one by one, and reports the ones that could not be invited.
     *
     * Passing them to `createRoom` instead would be a single request, but it fails as a whole: one participant on
     * an unreachable server, or a mistyped id the user picked out of the search results, and the homeserver answers
     * 502 with the event nowhere to be found. The schedule the organiser typed is worth more than an invite, so the
     * room is created first and the invites are sent after it, each on its own.
     */
    private suspend fun inviteParticipants(roomId: RoomId, participants: List<UserId>): ImmutableList<UserId> {
        if (participants.isEmpty()) return persistentListOf()
        val room = client.getJoinedRoom(roomId)
        if (room == null) {
            Timber.w("The room of the event is not available yet, nobody was invited")
            return participants.toImmutableList()
        }
        return room.use {
            participants.filter { participant ->
                room.inviteUserById(participant)
                    .onFailure { Timber.w(it, "Could not invite $participant to the event") }
                    .isFailure
            }
        }.toImmutableList()
    }
}

/**
 * The schedule to store for this form, in [zoneId].
 *
 * The form keeps an inclusive end date for an all-day event, the way a user reads it, while a schedule ends at the
 * exclusive midnight that closes the last day.
 */
internal fun NewEventForm.toSchedule(zoneId: ZoneId): CalendarSchedule {
    val start = if (isAllDay) start.toLocalDate().atStartOfDay() else start
    val end = if (isAllDay) end.toLocalDate().plusDays(1).atStartOfDay() else end
    return CalendarSchedule(
        start = start,
        end = end,
        timeZone = zoneId,
        isAllDay = isAllDay,
        rrule = recurrence.toRRule(start.toLocalDate()),
    )
}

/**
 * The schedule to store for this form in [zoneId], when the form shows its times in [deviceZone].
 *
 * Used when an existing event is edited, so it keeps the time zone it was created in: an unchanged time then stays
 * exactly the same wall-clock time, which is what the per-occurrence changes are keyed on. An all-day event is a
 * whole date rather than an instant, so its dates are kept as they are.
 */
internal fun NewEventForm.toSchedule(zoneId: ZoneId, deviceZone: ZoneId): CalendarSchedule {
    val inZone = if (isAllDay) this else copy(start = start.moveZone(deviceZone, zoneId), end = end.moveZone(deviceZone, zoneId))
    return inZone.toSchedule(zoneId)
}
