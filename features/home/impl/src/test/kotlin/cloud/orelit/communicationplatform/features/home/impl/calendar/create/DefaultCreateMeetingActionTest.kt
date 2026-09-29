/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.create

import cloud.orelit.communicationplatform.features.enterprise.test.FakeSessionEnterpriseService
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMembershipState
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EXCEPTION
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeBaseRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeJoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.room.aRoomMember
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import java.time.ZoneId
import java.util.Locale

class DefaultCreateMeetingActionTest {
    private val botId = "@bot:server.org"
    private val form = aNewEventForm(title = "Meeting")

    private data class SentEvent(val type: String, val content: String)

    private fun aBotRoom(
        membershipOf: () -> RoomMembershipState = { RoomMembershipState.JOIN },
        onSend: (SentEvent) -> Result<Unit> = { Result.success(Unit) },
    ) = FakeJoinedRoom(
        baseRoom = FakeBaseRoom(
            roomId = A_ROOM_ID,
            getUpdatedMemberResult = { Result.success(aRoomMember(userId = UserId(botId), membership = membershipOf())) },
        ),
        sendRawResult = { type, content -> onSend(SentEvent(type, content)) },
    )

    private fun anAction(
        client: FakeMatrixClient,
        isEncryptionDisabled: Boolean = false,
    ) = DefaultCreateMeetingAction(
        client = client,
        sessionEnterpriseService = FakeSessionEnterpriseService(isEncryptionDisabledResult = { isEncryptionDisabled }),
    )

    private suspend fun DefaultCreateMeetingAction.run(botUserId: String? = botId) = execute(
        form = form,
        botUserId = botUserId,
        uid = "a-uid",
        zoneId = ZoneId.of("Europe/Berlin"),
        locale = Locale.ENGLISH,
    )

    @Test
    fun `without a configured bot nothing can be created`() = runTest {
        val result = anAction(FakeMatrixClient()).run(botUserId = null)
        assertThat(result.exceptionOrNull()).isInstanceOf(MeetingsNotConfiguredException::class.java)
    }

    @Test
    fun `the request is sent to the existing chat with the bot`() = runTest {
        var sent: SentEvent? = null
        val client = FakeMatrixClient().apply {
            givenFindDmResult(Result.success(A_ROOM_ID))
            givenGetRoomResult(
                A_ROOM_ID,
                aBotRoom(
                    onSend = {
                        sent = it
                        Result.success(Unit)
                    }
                ),
            )
        }
        val result = anAction(client).run()
        assertThat(result.isSuccess).isTrue()
        val event = sent
        assertThat(event?.type).isEqualTo("net.nordeck.meetings.meeting.create")
        val json = Json.parseToJsonElement(event!!.content).jsonObject
        assertThat(json.getValue("data").jsonObject.getValue("title").jsonPrimitive.content).isEqualTo("Meeting")
        assertThat(json.getValue("context").jsonObject.getValue("timezone").jsonPrimitive.content).isEqualTo("Europe/Berlin")
    }

    @Test
    fun `a chat with the bot is created when there is none`() = runTest {
        val newRoomId = RoomId("!new:server.org")
        var createdFor: UserId? = null
        var sent: SentEvent? = null
        val client = FakeMatrixClient().apply {
            givenFindDmResult(Result.success(null))
            createDmResult = { userId, _ ->
                createdFor = userId
                Result.success(newRoomId)
            }
            givenGetRoomResult(
                newRoomId,
                FakeJoinedRoom(
                    baseRoom = FakeBaseRoom(
                        roomId = newRoomId,
                        getUpdatedMemberResult = { Result.success(aRoomMember(userId = UserId(botId))) },
                    ),
                    sendRawResult = { type, content ->
                        sent = SentEvent(type, content)
                        Result.success(Unit)
                    },
                )
            )
        }
        val result = anAction(client).run()
        assertThat(result.isSuccess).isTrue()
        assertThat(createdFor).isEqualTo(UserId(botId))
        assertThat(sent).isNotNull()
    }

    @Test
    fun `the chat with the bot is encrypted unless the homeserver forbids it`() = runTest {
        suspend fun isEncryptedWhen(isEncryptionDisabled: Boolean): Boolean? {
            var encrypted: Boolean? = null
            val client = FakeMatrixClient().apply {
                givenFindDmResult(Result.success(null))
                createDmResult = { _, isEncrypted ->
                    encrypted = isEncrypted
                    Result.success(A_ROOM_ID)
                }
                givenGetRoomResult(A_ROOM_ID, aBotRoom())
            }
            anAction(client, isEncryptionDisabled).run()
            return encrypted
        }
        assertThat(isEncryptedWhen(isEncryptionDisabled = false)).isTrue()
        assertThat(isEncryptedWhen(isEncryptionDisabled = true)).isFalse()
    }

    @Test
    fun `a failure to find the chat is reported`() = runTest {
        val client = FakeMatrixClient().apply { givenFindDmResult(Result.failure(AN_EXCEPTION)) }
        assertThat(anAction(client).run().exceptionOrNull()).isEqualTo(AN_EXCEPTION)
    }

    @Test
    fun `a failure to create the chat is reported`() = runTest {
        val client = FakeMatrixClient().apply {
            givenFindDmResult(Result.success(null))
            givenCreateDmResult(Result.failure(AN_EXCEPTION))
        }
        assertThat(anAction(client).run().exceptionOrNull()).isEqualTo(AN_EXCEPTION)
    }

    @Test
    fun `a chat that is not available is reported`() = runTest {
        val client = FakeMatrixClient().apply { givenFindDmResult(Result.success(A_ROOM_ID)) }
        assertThat(anAction(client).run().isFailure).isTrue()
    }

    @Test
    fun `a bot that never joins the chat is reported`() = runTest {
        var isSent = false
        val client = FakeMatrixClient().apply {
            givenFindDmResult(Result.success(A_ROOM_ID))
            givenGetRoomResult(
                A_ROOM_ID,
                aBotRoom(
                    membershipOf = { RoomMembershipState.INVITE },
                    onSend = {
                        isSent = true
                        Result.success(Unit)
                    },
                ),
            )
        }
        val result = anAction(client).run()
        assertThat(result.exceptionOrNull()).isInstanceOf(MeetingsBotUnavailableException::class.java)
        assertThat(isSent).isFalse()
    }

    @Test
    fun `the request waits for the bot to join the chat`() = runTest {
        var checks = 0
        var sentCount = 0
        val client = FakeMatrixClient().apply {
            givenFindDmResult(Result.success(A_ROOM_ID))
            givenGetRoomResult(
                A_ROOM_ID,
                aBotRoom(
                    membershipOf = { if (++checks < 4) RoomMembershipState.INVITE else RoomMembershipState.JOIN },
                    onSend = {
                        sentCount++
                        Result.success(Unit)
                    },
                ),
            )
        }
        assertThat(anAction(client).run().isSuccess).isTrue()
        assertThat(checks).isEqualTo(4)
        assertThat(sentCount).isEqualTo(1)
    }

    @Test
    fun `a failure to send the request is reported`() = runTest {
        val client = FakeMatrixClient().apply {
            givenFindDmResult(Result.success(A_ROOM_ID))
            givenGetRoomResult(A_ROOM_ID, aBotRoom(onSend = { Result.failure(AN_EXCEPTION) }))
        }
        assertThat(anAction(client).run().exceptionOrNull()).isEqualTo(AN_EXCEPTION)
    }
}
