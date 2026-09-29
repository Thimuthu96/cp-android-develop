/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.textcomposer.mentions

import androidx.compose.runtime.Immutable
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMember
import cloud.orelit.communicationplatform.libraries.slashcommands.api.SlashCommandSuggestion

@Immutable
sealed interface ResolvedSuggestion {
    data object AtRoom : ResolvedSuggestion
    data class Member(val roomMember: RoomMember) : ResolvedSuggestion
    data class Alias(
        val roomAlias: RoomAlias,
        val roomId: RoomId,
        val roomName: String?,
        val roomAvatarUrl: String?,
    ) : ResolvedSuggestion {
        fun getAvatarData(size: AvatarSize) = AvatarData(
            id = roomId.value,
            name = roomName,
            url = roomAvatarUrl,
            size = size,
        )
    }

    data class Command(
        val command: SlashCommandSuggestion,
    ) : ResolvedSuggestion
}
