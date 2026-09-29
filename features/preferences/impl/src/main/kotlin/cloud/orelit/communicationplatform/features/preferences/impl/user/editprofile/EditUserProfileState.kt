/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.user.editprofile

import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.AvatarAction
import cloud.orelit.communicationplatform.libraries.permissions.api.PermissionsState
import kotlinx.collections.immutable.ImmutableList

data class EditUserProfileState(
    val userId: UserId,
    val displayName: String,
    val userAvatarUrl: String?,
    val avatarActions: ImmutableList<AvatarAction>,
    val saveButtonEnabled: Boolean,
    val saveAction: AsyncAction<Unit>,
    val cameraPermissionState: PermissionsState,
    val canChangeDisplayName: Boolean,
    val canChangeAvatarUrl: Boolean,
    val eventSink: (EditUserProfileEvent) -> Unit
)
