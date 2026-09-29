/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.userprofile.shared

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.features.startchat.api.ConfirmingStartDmWithMatrixUser
import cloud.orelit.communicationplatform.features.userprofile.api.UserProfileEvent
import cloud.orelit.communicationplatform.features.userprofile.api.UserProfileState
import cloud.orelit.communicationplatform.features.userprofile.api.UserProfileVerificationState
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarMessage
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.user.DisplayedStatus
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.aMatrixUser

open class UserProfileStatePreviewParam : PreviewParameterProvider<UserProfileState> {
    override val values: Sequence<UserProfileState>
        get() = sequenceOf(
            aUserProfileState(),
            aUserProfileState(userName = null),
            aUserProfileState(isBlocked = AsyncData.Success(true), verificationState = UserProfileVerificationState.VERIFIED),
            aUserProfileState(displayConfirmationDialog = UserProfileState.ConfirmationDialog.Block),
            aUserProfileState(displayConfirmationDialog = UserProfileState.ConfirmationDialog.Unblock),
            aUserProfileState(isBlocked = AsyncData.Loading(true), verificationState = UserProfileVerificationState.UNKNOWN),
            aUserProfileState(startDmActionState = AsyncAction.Loading),
            aUserProfileState(canCall = true),
            aUserProfileState(startDmActionState = ConfirmingStartDmWithMatrixUser(aMatrixUser(), isUserIdentityUnknown = false)),
            aUserProfileState(verificationState = UserProfileVerificationState.VERIFICATION_VIOLATION),
        )
}

fun aUserProfileState(
    userId: UserId = UserId("@daniel:domain.com"),
    userName: String? = "Daniel",
    avatarUrl: String? = null,
    isBlocked: AsyncData<Boolean> = AsyncData.Success(false),
    verificationState: UserProfileVerificationState = UserProfileVerificationState.UNVERIFIED,
    startDmActionState: AsyncAction<RoomId> = AsyncAction.Uninitialized,
    displayConfirmationDialog: UserProfileState.ConfirmationDialog? = null,
    isCurrentUser: Boolean = false,
    dmRoomId: RoomId? = null,
    canCall: Boolean = false,
    snackbarMessage: SnackbarMessage? = null,
    displayedStatus: DisplayedStatus? = null,
    eventSink: (UserProfileEvent) -> Unit = {},
) = UserProfileState(
    userId = userId,
    userName = userName,
    avatarUrl = avatarUrl,
    isBlocked = isBlocked,
    verificationState = verificationState,
    startDmActionState = startDmActionState,
    displayConfirmationDialog = displayConfirmationDialog,
    isCurrentUser = isCurrentUser,
    dmRoomId = dmRoomId,
    canCall = canCall,
    snackbarMessage = snackbarMessage,
    displayedStatus = displayedStatus,
    eventSink = eventSink,
)
