/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomdetails.impl

import androidx.compose.runtime.Immutable
import cloud.orelit.communicationplatform.features.leaveroom.api.LeaveRoomState
import cloud.orelit.communicationplatform.features.roomcall.api.RoomCallState
import cloud.orelit.communicationplatform.features.userprofile.api.UserProfileState
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarMessage
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMember
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomNotificationSettings
import cloud.orelit.communicationplatform.libraries.matrix.api.room.history.RoomHistoryVisibility
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class RoomDetailsState(
    val roomId: RoomId,
    val roomName: String,
    val roomAlias: RoomAlias?,
    val roomAvatarUrl: String?,
    val roomTopic: RoomTopicState,
    val memberCount: Long,
    val isEncrypted: Boolean,
    val roomType: RoomDetailsType,
    val dmOtherMemberDetailsState: UserProfileState?,
    val canEdit: Boolean,
    val canInvite: Boolean,
    val roomCallState: RoomCallState,
    val leaveRoomState: LeaveRoomState,
    val roomNotificationSettings: RoomNotificationSettings?,
    val isFavorite: Boolean,
    val displayRolesAndPermissionsSettings: Boolean,
    val isPublic: Boolean,
    val heroes: ImmutableList<MatrixUser>,
    val pinnedMessagesCount: Int?,
    val snackbarMessage: SnackbarMessage?,
    val canShowKnockRequests: Boolean,
    val knockRequestsCount: Int?,
    val canShowSecurityAndPrivacy: Boolean,
    val hasMemberVerificationViolations: Boolean,
    val canReportRoom: Boolean,
    val isTombstoned: Boolean,
    val showDebugInfo: Boolean,
    val roomVersion: String?,
    val roomHistoryVisibility: RoomHistoryVisibility,
    val hasNewContent: Boolean,
    val eventSink: (RoomDetailsEvent) -> Unit
) {
    val roomBadges = buildList {
        if (isEncrypted) {
            add(RoomBadge.ENCRYPTED)
        } else {
            add(RoomBadge.NOT_ENCRYPTED)
        }
        if (isPublic) {
            add(RoomBadge.PUBLIC)
        }
        if (isEncrypted) {
            when (roomHistoryVisibility) {
                RoomHistoryVisibility.Invited, RoomHistoryVisibility.Joined -> add(RoomBadge.SHARED_HISTORY_HIDDEN)
                RoomHistoryVisibility.Shared -> add(RoomBadge.SHARED_HISTORY_SHARED)
                RoomHistoryVisibility.WorldReadable -> add(RoomBadge.SHARED_HISTORY_WORLD_READABLE)
                else -> {}
            }
        }
    }.toImmutableList()
}

@Immutable
sealed interface RoomDetailsType {
    data object Room : RoomDetailsType
    data class Dm(val otherMember: RoomMember) : RoomDetailsType
}

@Immutable
sealed interface RoomTopicState {
    data object Hidden : RoomTopicState
    data object CanAddTopic : RoomTopicState
    data class ExistingTopic(val topic: String) : RoomTopicState
}

enum class RoomBadge {
    ENCRYPTED,
    NOT_ENCRYPTED,
    PUBLIC,
    SHARED_HISTORY_HIDDEN,
    SHARED_HISTORY_SHARED,
    SHARED_HISTORY_WORLD_READABLE
}
