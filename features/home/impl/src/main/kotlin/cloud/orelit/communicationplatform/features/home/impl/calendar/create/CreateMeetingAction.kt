/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.create

import cloud.orelit.communicationplatform.appconfig.MeetingsConfig
import cloud.orelit.communicationplatform.features.enterprise.api.SessionEnterpriseService
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMembershipState
import cloud.orelit.communicationplatform.libraries.matrix.api.room.StartDMResult
import cloud.orelit.communicationplatform.libraries.matrix.api.room.startDM
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.time.ZoneId
import java.util.Locale
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Creates a meeting the way the meetings widget of the web app does: by sending a room message to the meetings bot,
 * which then creates the meeting room and invites the participants. See ADR001 of matrix-meetings.
 *
 * This is the opt-in path: it is used only by `CreateCalendarEventAction` when a deployment runs the bot, see
 * [MeetingsConfig.BOT_USER_ID]. Without one, the app creates the event's room itself.
 */
interface CreateMeetingAction {
    /** False when no meetings bot is configured, in which case [execute] always fails. */
    val isAvailable: Boolean

    /**
     * Sends the request to the bot. The bot works asynchronously, so a success only means that the request was sent:
     * the meeting is created a few moments later, and the user is invited to its room.
     */
    suspend fun execute(form: NewEventForm): Result<Unit>
}

/** No meetings bot is configured, see [MeetingsConfig.BOT_USER_ID]. */
class MeetingsNotConfiguredException : Exception("No meetings bot is configured")

/** The bot did not join the chat with the user in time, so it did not get the request. */
class MeetingsBotUnavailableException : Exception("The meetings bot did not join the chat")

@ContributesBinding(SessionScope::class)
class DefaultCreateMeetingAction(
    private val client: MatrixClient,
    private val sessionEnterpriseService: SessionEnterpriseService,
) : CreateMeetingAction {
    override val isAvailable: Boolean = MeetingsConfig.BOT_USER_ID != null

    override suspend fun execute(form: NewEventForm): Result<Unit> = execute(form, MeetingsConfig.BOT_USER_ID)

    internal suspend fun execute(
        form: NewEventForm,
        botUserId: String?,
        uid: String = UUID.randomUUID().toString(),
        zoneId: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault(),
    ): Result<Unit> {
        if (botUserId == null) return Result.failure(MeetingsNotConfiguredException())
        val bot = UserId(botUserId)
        val roomId = when (
            val result = client.startDM(
                userId = bot,
                createIfDmDoesNotExist = true,
                isEncryped = !sessionEnterpriseService.isEncryptionDisabledByHomeserver(),
            )
        ) {
            is StartDMResult.Success -> result.roomId
            is StartDMResult.Failure -> return Result.failure(result.throwable)
            // Cannot happen, the DM is created when it does not exist.
            StartDMResult.DmDoesNotExist -> return Result.failure(IllegalStateException("No chat with the meetings bot"))
        }
        val room = client.getJoinedRoom(roomId)
            ?: return Result.failure(IllegalStateException("The chat with the meetings bot is not available"))
        // A bot that has not joined yet would not see the request.
        if (!room.awaitJoined(bot)) return Result.failure(MeetingsBotUnavailableException())

        val content = form.toMeetingCreateContent(uid = uid, zoneId = zoneId, locale = locale)
        return room.sendRaw(MEETING_CREATE_EVENT_TYPE, content)
            .onFailure { Timber.e(it, "Could not send the meeting request to the meetings bot") }
    }

    private suspend fun JoinedRoom.awaitJoined(user: UserId): Boolean {
        return withTimeoutOrNull(BOT_JOIN_TIMEOUT) {
            while (getUpdatedMember(user).getOrNull()?.membership != RoomMembershipState.JOIN) {
                delay(BOT_JOIN_POLL_INTERVAL)
            }
            true
        } ?: false
    }

    private companion object {
        val BOT_JOIN_TIMEOUT = 30.seconds
        val BOT_JOIN_POLL_INTERVAL = 500.milliseconds
    }
}
