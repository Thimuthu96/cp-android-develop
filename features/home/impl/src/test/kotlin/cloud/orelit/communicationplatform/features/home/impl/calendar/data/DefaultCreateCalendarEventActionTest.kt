/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.data

import cloud.orelit.communicationplatform.features.enterprise.test.FakeSessionEnterpriseService
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.FakeCreateMeetingAction
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.aNewEventForm
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.createroom.CreateRoomParameters
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EXCEPTION
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeBaseRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeJoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.aMatrixUser
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.ZoneId

class DefaultCreateCalendarEventActionTest {
    private val alice = aMatrixUser(id = "@alice:example.org")
    private val bob = aMatrixUser(id = "@bob:example.org")

    @Test
    fun `the event is created with its schedule in the topic`() = runTest {
        var parameters: CreateRoomParameters? = null
        val client = FakeMatrixClient().apply {
            createRoomResult = {
                parameters = it
                Result.success(A_ROOM_ID)
            }
        }

        val result = anAction(client).execute(aNewEventForm(title = "Sprint planning"), ZoneId.of("Europe/Berlin"))

        assertThat(result.getOrThrow().roomId).isEqualTo(A_ROOM_ID)
        assertThat(parameters!!.name).isEqualTo("Sprint planning")
        val schedule = CalendarRoomEncoding.decodeSchedule(parameters!!.topic)
        assertThat(schedule).isNotNull()
        assertThat(schedule!!.timeZone).isEqualTo(ZoneId.of("Europe/Berlin"))
    }

    @Test
    fun `the participants are invited after the room exists, not through createRoom`() = runTest {
        var parameters: CreateRoomParameters? = null
        val invited = mutableListOf<UserId>()
        val client = FakeMatrixClient().apply {
            createRoomResult = {
                parameters = it
                Result.success(A_ROOM_ID)
            }
            givenGetRoomResult(A_ROOM_ID, aRoom {
                invited += it
                Result.success(Unit)
            })
        }

        val result = anAction(client).execute(aFormWith(alice, bob), ZoneId.systemDefault())

        // Passing them to createRoom would make one unreachable participant fail the whole event.
        assertThat(parameters!!.invite).isNull()
        assertThat(invited).containsExactly(alice.userId, bob.userId)
        assertThat(result.getOrThrow().notInvited).isEmpty()
    }

    @Test
    fun `a participant who cannot be invited does not lose the event`() = runTest {
        val client = FakeMatrixClient().apply {
            createRoomResult = { Result.success(A_ROOM_ID) }
            // Reproduces a mistyped id whose server cannot be reached, which answers 502.
            givenGetRoomResult(A_ROOM_ID, aRoom { if (it == bob.userId) Result.failure(AN_EXCEPTION) else Result.success(Unit) })
        }

        val created = anAction(client).execute(aFormWith(alice, bob), ZoneId.systemDefault()).getOrThrow()

        assertThat(created.roomId).isEqualTo(A_ROOM_ID)
        assertThat(created.notInvited).containsExactly(bob.userId)
    }

    @Test
    fun `a failure to create the room is reported`() = runTest {
        val client = FakeMatrixClient().apply { createRoomResult = { Result.failure(AN_EXCEPTION) } }

        val result = anAction(client).execute(aNewEventForm(title = "Sprint planning"), ZoneId.systemDefault())

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(AN_EXCEPTION)
    }

    @Test
    fun `nobody is invited when the new room is not available yet`() = runTest {
        val client = FakeMatrixClient().apply {
            createRoomResult = { Result.success(A_ROOM_ID) }
            givenGetRoomResult(A_ROOM_ID, null)
        }

        val created = anAction(client).execute(aFormWith(alice, bob), ZoneId.systemDefault()).getOrThrow()

        assertThat(created.roomId).isEqualTo(A_ROOM_ID)
        assertThat(created.notInvited).containsExactly(alice.userId, bob.userId)
    }

    private fun aFormWith(vararg participants: MatrixUser) =
        participants.fold(aNewEventForm(title = "Sprint planning")) { form, user -> form.withParticipant(user) }

    private fun aRoom(onInvite: (UserId) -> Result<Unit>) = FakeJoinedRoom(
        baseRoom = FakeBaseRoom(roomId = A_ROOM_ID),
        inviteUserResult = onInvite,
    )

    private fun anAction(client: FakeMatrixClient) = DefaultCreateCalendarEventAction(
        client = client,
        sessionEnterpriseService = FakeSessionEnterpriseService(isEncryptionDisabledResult = { false }),
        createMeetingAction = FakeCreateMeetingAction(),
    )
}
