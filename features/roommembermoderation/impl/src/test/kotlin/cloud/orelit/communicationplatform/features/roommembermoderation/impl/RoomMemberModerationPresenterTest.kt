/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roommembermoderation.impl

import app.cash.turbine.TurbineTestContext
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.roommembermoderation.api.ModerationAction
import cloud.orelit.communicationplatform.features.roommembermoderation.api.ModerationActionState
import cloud.orelit.communicationplatform.features.roommembermoderation.api.RoomMemberModerationEvent
import cloud.orelit.communicationplatform.features.roommembermoderation.api.RoomMemberModerationPermissions
import cloud.orelit.communicationplatform.features.roommembermoderation.api.RoomMemberModerationState
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.core.coroutine.CoroutineDispatchers
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMember
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMembersState
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMembershipState
import cloud.orelit.communicationplatform.libraries.matrix.api.room.powerlevels.RoomPowerLevels
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeBaseRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeJoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.room.aRoomInfo
import cloud.orelit.communicationplatform.libraries.matrix.test.room.aRoomMember
import cloud.orelit.communicationplatform.libraries.matrix.test.room.defaultRoomPowerLevelValues
import cloud.orelit.communicationplatform.libraries.matrix.test.room.powerlevels.FakeRoomPermissions
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService
import cloud.orelit.communicationplatform.services.analytics.test.FakeAnalyticsService
import cloud.orelit.communicationplatform.tests.testutils.WarmUpRule
import cloud.orelit.communicationplatform.tests.testutils.test
import cloud.orelit.communicationplatform.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class RoomMemberModerationPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val targetUser = MatrixUser(userId = A_USER_ID)

    @Test
    fun `present - initial state`() = runTest {
        val room = aJoinedRoom()
        createRoomMemberModerationPresenter(room = room).test {
            val initialState = awaitState()
            assertThat(initialState.permissions).isEqualTo(RoomMemberModerationPermissions.DEFAULT)
            assertThat(initialState.selectedUser).isNull()
            assertThat(initialState.banUserAsyncAction).isEqualTo(AsyncAction.Uninitialized)
            assertThat(initialState.kickUserAsyncAction).isEqualTo(AsyncAction.Uninitialized)
            assertThat(initialState.unbanUserAsyncAction).isEqualTo(AsyncAction.Uninitialized)
            assertThat(initialState.actions).isEmpty()
        }
    }

    @Test
    fun `present - show actions when canBan=false, canKick=false`() = runTest {
        val room = aJoinedRoom(
            canBan = false,
            canKick = false,
            myUserRole = RoomMember.Role.User,
            targetRoomMember = aRoomMember(userId = A_USER_ID, powerLevel = RoomMember.Role.User.powerLevel)
        )
        createRoomMemberModerationPresenter(room = room).test {
            val initialState = awaitState()
            initialState.eventSink(RoomMemberModerationEvent.ShowActionsForUser(targetUser))
            skipItems(1)
            val updatedState = awaitState()
            assertThat(updatedState.selectedUser).isEqualTo(targetUser)
            assertThat(updatedState.actions).containsExactly(
                ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
            )
        }
    }

    @Test
    fun `present - show actions when canBan=true, canKick=true, userRole=Admin and target member is unknown`() = runTest {
        val room = aJoinedRoom(
            canBan = true,
            canKick = true,
            myUserRole = RoomMember.Role.Admin,
            targetRoomMember = null
        )
        createRoomMemberModerationPresenter(room = room).test {
            val initialState = awaitState()
            initialState.eventSink(RoomMemberModerationEvent.ShowActionsForUser(targetUser))
            skipItems(2)
            val updatedState = awaitState()
            assertThat(updatedState.selectedUser).isEqualTo(targetUser)
            assertThat(updatedState.actions).containsExactly(
                ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
                ModerationActionState(action = ModerationAction.KickUser, isEnabled = true),
                ModerationActionState(action = ModerationAction.BanUser, isEnabled = true),
            )
        }
    }

    @Test
    fun `show actions when canBan=true, canKick=true, userRole=Admin and target is User`() = runTest {
        val room = aJoinedRoom(
            canBan = true,
            canKick = true,
            myUserRole = RoomMember.Role.Admin,
            targetRoomMember = aRoomMember(userId = A_USER_ID, powerLevel = RoomMember.Role.User.powerLevel)
        )
        createRoomMemberModerationPresenter(room = room).test {
            val initialState = awaitState()
            initialState.eventSink(RoomMemberModerationEvent.ShowActionsForUser(targetUser))
            skipItems(2)
            val updatedState = awaitState()
            assertThat(updatedState.selectedUser).isEqualTo(targetUser)
            assertThat(updatedState.actions).containsExactly(
                ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
                ModerationActionState(action = ModerationAction.KickUser, isEnabled = true),
                ModerationActionState(action = ModerationAction.BanUser, isEnabled = true),
            )
        }
    }

    @Test
    fun `show actions when canBan=true, canKick=true, userRole=Moderator and target is Admin`() = runTest {
        val room = aJoinedRoom(
            canBan = true,
            canKick = true,
            myUserRole = RoomMember.Role.Moderator,
            targetRoomMember = aRoomMember(userId = A_USER_ID, powerLevel = RoomMember.Role.Admin.powerLevel)
        )
        createRoomMemberModerationPresenter(room = room).test {
            val initialState = awaitState()
            initialState.eventSink(RoomMemberModerationEvent.ShowActionsForUser(targetUser))
            skipItems(2)
            val updatedState = awaitState()
            assertThat(updatedState.selectedUser).isEqualTo(targetUser)
            assertThat(updatedState.actions).containsExactly(
                ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
                ModerationActionState(action = ModerationAction.KickUser, isEnabled = false),
                ModerationActionState(action = ModerationAction.BanUser, isEnabled = false),
            )
        }
    }

    @Test
    fun `show actions when canBan=true, canKick=true, userRole=Moderator and target is Banned`() = runTest {
        val room = aJoinedRoom(
            canBan = true,
            canKick = true,
            myUserRole = RoomMember.Role.Moderator,
            targetRoomMember = aRoomMember(userId = A_USER_ID, membership = RoomMembershipState.BAN)
        )
        createRoomMemberModerationPresenter(room = room).test {
            val initialState = awaitState()
            initialState.eventSink(RoomMemberModerationEvent.ShowActionsForUser(targetUser))
            skipItems(2)
            val updatedState = awaitState()
            assertThat(updatedState.selectedUser).isEqualTo(targetUser)
            assertThat(updatedState.actions).containsExactly(
                ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
                ModerationActionState(action = ModerationAction.UnbanUser, isEnabled = true),
            )
        }
    }

    @Test
    fun `present - process kick action sets confirming state`() = runTest {
        createRoomMemberModerationPresenter(room = aJoinedRoom()).test {
            val initialState = awaitState()
            initialState.eventSink(
                RoomMemberModerationEvent.ProcessAction(
                    targetUser = targetUser,
                    action = ModerationAction.KickUser
                )
            )
            skipItems(1)
            val updatedState = awaitState()
            assertThat(updatedState.selectedUser).isEqualTo(targetUser)
            assertThat(updatedState.kickUserAsyncAction).isEqualTo(AsyncAction.ConfirmingNoParams)
        }
    }

    @Test
    fun `present - process ban action sets confirming state`() = runTest {
        createRoomMemberModerationPresenter(room = aJoinedRoom()).test {
            val initialState = awaitState()
            initialState.eventSink(
                RoomMemberModerationEvent.ProcessAction(
                    targetUser = targetUser,
                    action = ModerationAction.BanUser
                )
            )
            skipItems(1)
            val updatedState = awaitState()
            assertThat(updatedState.selectedUser).isEqualTo(targetUser)
            assertThat(updatedState.banUserAsyncAction).isEqualTo(AsyncAction.ConfirmingNoParams)
        }
    }

    @Test
    fun `present - process unban action sets confirming state`() = runTest {
        createRoomMemberModerationPresenter(room = aJoinedRoom()).test {
            val initialState = awaitState()
            initialState.eventSink(
                RoomMemberModerationEvent.ProcessAction(
                    targetUser = targetUser,
                    action = ModerationAction.UnbanUser
                )
            )
            skipItems(1)
            val updatedState = awaitState()
            assertThat(updatedState.selectedUser).isEqualTo(targetUser)
            assertThat(updatedState.unbanUserAsyncAction).isEqualTo(AsyncAction.ConfirmingNoParams)
        }
    }

    @Test
    fun `present - do kick user with success`() = runTest {
        val room = aJoinedRoom()
        room.baseRoom.givenUpdateMembersResult {
            // Simulate the member list being updated
            room.givenRoomMembersState(
                RoomMembersState.Ready(
                    persistentListOf(aRoomMember())
                )
            )
        }
        createRoomMemberModerationPresenter(room = room).test {
            val initialState = awaitState()
            initialState.eventSink(
                RoomMemberModerationEvent.ProcessAction(
                    targetUser = targetUser,
                    action = ModerationAction.KickUser
                )
            )
            skipItems(2)
            initialState.eventSink(InternalRoomMemberModerationEvent.DoKickUser("Reason"))
            skipItems(1)
            val loadingState = awaitState()
            assertThat(loadingState.kickUserAsyncAction).isInstanceOf(AsyncAction.Loading::class.java)
            val successState = awaitState()
            assertThat(successState.kickUserAsyncAction).isInstanceOf(AsyncAction.Success::class.java)
            assertThat(successState.selectedUser).isNull()
        }
    }

    @Test
    fun `present - do ban user with success`() = runTest {
        val room = aJoinedRoom()
        room.baseRoom.givenUpdateMembersResult {
            // Simulate the member list being updated
            room.givenRoomMembersState(
                RoomMembersState.Ready(
                    persistentListOf(aRoomMember())
                )
            )
        }
        createRoomMemberModerationPresenter(room = room).test {
            val initialState = awaitState()
            initialState.eventSink(
                RoomMemberModerationEvent.ProcessAction(
                    targetUser = targetUser,
                    action = ModerationAction.BanUser
                )
            )
            skipItems(2)
            initialState.eventSink(InternalRoomMemberModerationEvent.DoBanUser("Reason"))
            skipItems(1)
            val loadingState = awaitState()
            assertThat(loadingState.banUserAsyncAction).isInstanceOf(AsyncAction.Loading::class.java)
            val successState = awaitState()
            assertThat(successState.banUserAsyncAction).isInstanceOf(AsyncAction.Success::class.java)
            assertThat(successState.selectedUser).isNull()
        }
    }

    @Test
    fun `present - do unban user with success`() = runTest {
        val room = aJoinedRoom()
        room.baseRoom.givenUpdateMembersResult {
            // Simulate the member list being updated
            room.givenRoomMembersState(
                RoomMembersState.Ready(
                    persistentListOf(aRoomMember())
                )
            )
        }
        createRoomMemberModerationPresenter(room = room).test {
            val initialState = awaitState()
            initialState.eventSink(
                RoomMemberModerationEvent.ProcessAction(
                    targetUser = targetUser,
                    action = ModerationAction.UnbanUser
                )
            )
            skipItems(2)
            initialState.eventSink(InternalRoomMemberModerationEvent.DoUnbanUser("Reason"))
            skipItems(1)
            val loadingState = awaitState()
            assertThat(loadingState.unbanUserAsyncAction).isInstanceOf(AsyncAction.Loading::class.java)
            val successState = awaitState()
            assertThat(successState.unbanUserAsyncAction).isInstanceOf(AsyncAction.Success::class.java)
            assertThat(successState.selectedUser).isNull()
        }
    }

    @Test
    fun `present - do kick user with failure`() = runTest {
        val error = RuntimeException("Test error")
        val room = aJoinedRoom(
            kickUserResult = Result.failure(error),
        )
        createRoomMemberModerationPresenter(room = room).test {
            val initialState = awaitState()
            initialState.eventSink(
                RoomMemberModerationEvent.ProcessAction(
                    targetUser = targetUser,
                    action = ModerationAction.KickUser
                )
            )
            skipItems(2)
            initialState.eventSink(InternalRoomMemberModerationEvent.DoKickUser("Reason"))
            skipItems(1)
            val loadingState = awaitState()
            assertThat(loadingState.kickUserAsyncAction).isInstanceOf(AsyncAction.Loading::class.java)
            val failureState = awaitState()
            assertThat(failureState.kickUserAsyncAction).isInstanceOf(AsyncAction.Failure::class.java)
        }
    }

    @Test
    fun `present - reset clears all async actions and selected user`() = runTest {
        createRoomMemberModerationPresenter(room = aJoinedRoom()).test {
            val initialState = awaitState()
            initialState.eventSink(
                RoomMemberModerationEvent.ProcessAction(targetUser = targetUser, action = ModerationAction.BanUser)
            )
            skipItems(2)
            initialState.eventSink(InternalRoomMemberModerationEvent.Reset)
            skipItems(1)
            val resetState = awaitState()
            assertThat(resetState.selectedUser).isNull()
            assertThat(resetState.banUserAsyncAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    private fun aJoinedRoom(
        canKick: Boolean = false,
        canBan: Boolean = false,
        myUserRole: RoomMember.Role = RoomMember.Role.User,
        kickUserResult: Result<Unit> = Result.success(Unit),
        banUserResult: Result<Unit> = Result.success(Unit),
        unBanUserResult: Result<Unit> = Result.success(Unit),
        targetRoomMember: RoomMember? = null,
    ): FakeJoinedRoom {
        return FakeJoinedRoom(
            kickUserResult = { _, _ -> kickUserResult },
            banUserResult = { _, _ -> banUserResult },
            unBanUserResult = { _, _ -> unBanUserResult },
            baseRoom = FakeBaseRoom(
                roomPermissions = FakeRoomPermissions(
                    canBan = canBan,
                    canKick = canKick
                ),
                userRoleResult = { Result.success(myUserRole) },
                updateMembersResult = { Result.success(Unit) },
                initialRoomInfo = aRoomInfo(
                    roomPowerLevels = RoomPowerLevels(
                        values = defaultRoomPowerLevelValues(),
                        users = persistentMapOf(A_USER_ID to myUserRole.powerLevel)
                    )
                )
            ),
        ).apply {
            val roomMembers = listOfNotNull(targetRoomMember).toImmutableList()
            givenRoomMembersState(state = RoomMembersState.Ready(roomMembers))
        }
    }

    private fun TestScope.createRoomMemberModerationPresenter(
        room: JoinedRoom,
        dispatchers: CoroutineDispatchers = testCoroutineDispatchers(),
        analyticsService: AnalyticsService = FakeAnalyticsService(),
    ): RoomMemberModerationPresenter {
        return RoomMemberModerationPresenter(
            room = room,
            dispatchers = dispatchers,
            analyticsService = analyticsService,
        )
    }

    private suspend fun TurbineTestContext<RoomMemberModerationState>.awaitState(): InternalRoomMemberModerationState {
        return awaitItem() as InternalRoomMemberModerationState
    }
}
