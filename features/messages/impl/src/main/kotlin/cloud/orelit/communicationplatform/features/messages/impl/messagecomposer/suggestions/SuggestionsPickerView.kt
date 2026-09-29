/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.messagecomposer.suggestions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.features.messages.impl.R
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.Avatar
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarType
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarType.Room
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.anAvatarData
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_BOB
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.HorizontalDivider
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMember
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMembershipState
import cloud.orelit.communicationplatform.libraries.matrix.api.room.getBestName
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.DisplayNameWithStatus
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getAvatarData
import cloud.orelit.communicationplatform.libraries.slashcommands.api.SlashCommandSuggestion
import cloud.orelit.communicationplatform.libraries.textcomposer.mentions.ResolvedSuggestion
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun SuggestionsPickerView(
    roomId: RoomId,
    roomName: String?,
    roomAvatarData: AvatarData,
    suggestions: ImmutableList<ResolvedSuggestion>,
    onSelectSuggestion: (ResolvedSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
    ) {
        items(
            suggestions,
            key = { suggestion ->
                when (suggestion) {
                    is ResolvedSuggestion.AtRoom -> "@room"
                    is ResolvedSuggestion.Member -> suggestion.roomMember.userId.value
                    is ResolvedSuggestion.Alias -> suggestion.roomId.value
                    is ResolvedSuggestion.Command -> suggestion.command.command
                }
            }
        ) {
            Column(modifier = Modifier.fillParentMaxWidth()) {
                SuggestionItemView(
                    suggestion = it,
                    roomId = roomId.value,
                    roomName = roomName,
                    roomAvatar = roomAvatarData,
                    onSelectSuggestion = onSelectSuggestion,
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun SuggestionItemView(
    suggestion: ResolvedSuggestion,
    roomId: String,
    roomName: String?,
    roomAvatar: AvatarData?,
    onSelectSuggestion: (ResolvedSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable { onSelectSuggestion(suggestion) }
            .padding(horizontal = 16.dp),
    ) {
        val avatarSize = AvatarSize.Suggestion
        val avatarData = when (suggestion) {
            is ResolvedSuggestion.AtRoom -> roomAvatar?.copy(size = avatarSize) ?: AvatarData(roomId, roomName, null, avatarSize)
            is ResolvedSuggestion.Member -> suggestion.roomMember.getAvatarData(avatarSize)
            is ResolvedSuggestion.Alias -> suggestion.getAvatarData(avatarSize)
            is ResolvedSuggestion.Command -> null
        }
        val avatarType = when (suggestion) {
            is ResolvedSuggestion.Alias -> Room()
            ResolvedSuggestion.AtRoom,
            is ResolvedSuggestion.Member -> AvatarType.User
            is ResolvedSuggestion.Command -> null
        }
        val title: String? = when (suggestion) {
            is ResolvedSuggestion.AtRoom -> stringResource(R.string.screen_room_mentions_at_room_title)
            is ResolvedSuggestion.Member -> suggestion.roomMember.getBestName()
            is ResolvedSuggestion.Alias -> suggestion.roomName
            is ResolvedSuggestion.Command -> suggestion.command.command
        }
        val status = when (suggestion) {
            is ResolvedSuggestion.Member -> suggestion.roomMember.displayedStatus
            else -> null
        }
        val details = when (suggestion) {
            is ResolvedSuggestion.AtRoom,
            is ResolvedSuggestion.Member,
            is ResolvedSuggestion.Alias -> null
            is ResolvedSuggestion.Command -> suggestion.command.parameters
        }
        val subtitle = when (suggestion) {
            is ResolvedSuggestion.AtRoom -> "@room"
            is ResolvedSuggestion.Member -> suggestion.roomMember.userId.value
            is ResolvedSuggestion.Alias -> suggestion.roomAlias.value
            is ResolvedSuggestion.Command -> suggestion.command.description
        }
        if (avatarData != null && avatarType != null) {
            Avatar(
                avatarData = avatarData,
                avatarType = avatarType,
                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp, end = 16.dp),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp)
                .align(Alignment.CenterVertically),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                title?.let {
                    DisplayNameWithStatus(
                        name = title,
                        status = status,
                        style = ElementTheme.typography.fontBodyLgRegular,
                        nameColor = ElementTheme.colors.textPrimary,
                    )
                }
                details?.let {
                    Text(
                        text = it,
                        style = ElementTheme.typography.fontBodyMdRegular,
                        maxLines = 1,
                        color = ElementTheme.colors.textSecondary,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = subtitle,
                style = ElementTheme.typography.fontBodySmRegular,
                color = ElementTheme.colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun SuggestionsPickerViewPreview() {
    ElementPreview {
        val roomMember = RoomMember(
            userId = UserId("@alice:server.org"),
            displayName = null,
            avatarUrl = null,
            membership = RoomMembershipState.JOIN,
            isNameAmbiguous = false,
            powerLevel = 0L,
            isIgnored = false,
            role = RoomMember.Role.User,
            membershipChangeReason = null,
            isServiceMember = false,
            displayedStatus = null,
        )
        val anAlias = remember { RoomAlias("#room:domain.org") }
        SuggestionsPickerView(
            roomId = RoomId("!room:matrix.org"),
            roomName = "Room",
            roomAvatarData = anAvatarData(),
            suggestions = persistentListOf(
                ResolvedSuggestion.AtRoom,
                ResolvedSuggestion.Member(roomMember),
                ResolvedSuggestion.Member(roomMember.copy(userId = UserId("@bob:server.org"), displayName = USER_NAME_BOB)),
                ResolvedSuggestion.Alias(
                    roomAlias = anAlias,
                    roomId = RoomId("!room:matrix.org"),
                    roomName = "My room",
                    roomAvatarUrl = null,
                ),
                ResolvedSuggestion.Command(
                    command = SlashCommandSuggestion(
                        command = "/noparam",
                        parameters = null,
                        description = "A slash command without parameters",
                    )
                ),
                ResolvedSuggestion.Command(
                    command = SlashCommandSuggestion(
                        command = "/withparam",
                        parameters = "<user-id> [reason]",
                        description = "A slash command with parameters",
                    )
                ),
            ),
            onSelectSuggestion = {}
        )
    }
}
