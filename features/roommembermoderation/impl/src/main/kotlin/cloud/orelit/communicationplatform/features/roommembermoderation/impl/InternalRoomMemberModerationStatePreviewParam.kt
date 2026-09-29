/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roommembermoderation.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.features.roommembermoderation.api.ModerationAction
import cloud.orelit.communicationplatform.features.roommembermoderation.api.ModerationActionState
import cloud.orelit.communicationplatform.features.roommembermoderation.api.RoomMemberModerationEvent
import cloud.orelit.communicationplatform.features.roommembermoderation.api.RoomMemberModerationPermissions
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_ALICE
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.user.DisplayedStatus
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.toImmutableList

class InternalRoomMemberModerationStatePreviewParam : PreviewParameterProvider<InternalRoomMemberModerationState> {
    override val values: Sequence<InternalRoomMemberModerationState>
        get() = sequenceOf(
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                actions = listOf(
                    ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
                ),
            ),
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                actions = listOf(
                    ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
                    ModerationActionState(action = ModerationAction.KickUser, isEnabled = true),
                ),
            ),
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                actions = listOf(
                    ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
                    ModerationActionState(action = ModerationAction.KickUser, isEnabled = false),
                    ModerationActionState(action = ModerationAction.BanUser, isEnabled = true),
                    ),
            ),
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                actions = listOf(
                    ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
                    ModerationActionState(action = ModerationAction.KickUser, isEnabled = false),
                    ModerationActionState(action = ModerationAction.UnbanUser, isEnabled = true),
                ),
            ),
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                kickUserAsyncAction = AsyncAction.ConfirmingNoParams,
            ),
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                kickUserAsyncAction = AsyncAction.Loading,
            ),
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                banUserAsyncAction = AsyncAction.ConfirmingNoParams,
            ),
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                banUserAsyncAction = AsyncAction.Loading,
            ),
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                unbanUserAsyncAction = AsyncAction.ConfirmingNoParams,
            ),
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                unbanUserAsyncAction = AsyncAction.Loading,
            ),
        )
}

fun anAlice() = MatrixUser(
    userId = UserId(value = "@alice:server.org"),
    displayName = USER_NAME_ALICE,
    avatarUrl = null,
    displayedStatus = DisplayedStatus.InCall(0L),
)

fun aRoomMembersModerationState(
    permissions: RoomMemberModerationPermissions = RoomMemberModerationPermissions.DEFAULT,
    selectedUser: MatrixUser? = null,
    actions: List<ModerationActionState> = emptyList(),
    kickUserAsyncAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    banUserAsyncAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    unbanUserAsyncAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (RoomMemberModerationEvent) -> Unit = {},
) = InternalRoomMemberModerationState(
    permissions = permissions,
    selectedUser = selectedUser,
    actions = actions.toImmutableList(),
    kickUserAsyncAction = kickUserAsyncAction,
    banUserAsyncAction = banUserAsyncAction,
    unbanUserAsyncAction = unbanUserAsyncAction,
    eventSink = eventSink,
)
