/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.matrix.api.spaces.SpaceRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.spaces.SpaceRoomVisibility
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

fun SpaceRoom.getAvatarData(size: AvatarSize) = AvatarData(
    id = roomId.value,
    name = displayName,
    url = avatarUrl,
    size = size,
)

val SpaceRoomVisibility.icon: ImageVector
    @Composable
    get() {
        return when (this) {
            SpaceRoomVisibility.Private -> CompoundIcons.LockSolid()
            SpaceRoomVisibility.Public -> CompoundIcons.Public()
            SpaceRoomVisibility.SpaceMembers -> CompoundIcons.Space()
        }
    }

val SpaceRoomVisibility.label: String
    @Composable
    @ReadOnlyComposable
    get() {
        return when (this) {
            SpaceRoomVisibility.Private -> stringResource(CommonStrings.common_private)
            SpaceRoomVisibility.Public -> stringResource(CommonStrings.common_public)
            SpaceRoomVisibility.SpaceMembers -> stringResource(CommonStrings.common_space_members)
        }
    }
