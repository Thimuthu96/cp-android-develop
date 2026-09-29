/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.spaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.pages.HeaderFooterPage
import cloud.orelit.communicationplatform.libraries.designsystem.components.BigIcon
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Button
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.HorizontalDivider
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TextButton
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.CurrentUserMembership
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.SpaceHeaderRootView
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.SpaceHeaderView
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.SpaceRoomItemView
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getAvatarData
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList

@Composable
fun HomeSpacesView(
    state: HomeSpacesState,
    lazyListState: LazyListState,
    contentPadding: PaddingValues,
    onSpaceClick: (RoomId) -> Unit,
    onCreateSpaceClick: () -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.spaceRooms.isEmpty()) {
        EmptySpaceHomeView(
            modifier = modifier.padding(contentPadding),
            onCreateSpaceClick = onCreateSpaceClick,
            onExploreClick = onExploreClick,
            canExploreSpaces = state.canExploreSpaces,
        )
    } else {
        LazyColumn(
            modifier = modifier,
            state = lazyListState,
            contentPadding = contentPadding,
        ) {
            val space = state.space
            when (space) {
                CurrentSpace.Root -> {
                    item {
                        SpaceHeaderRootView(numberOfSpaces = state.spaceRooms.size)
                    }
                }
                is CurrentSpace.Space -> {
                    item {
                        SpaceHeaderView(
                            avatarData = space.spaceRoom.getAvatarData(AvatarSize.SpaceHeader),
                            alias = space.spaceRoom.canonicalAlias,
                            name = space.spaceRoom.displayName,
                            topic = space.spaceRoom.topic,
                            visibility = space.spaceRoom.visibility,
                            heroes = space.spaceRoom.heroes.toImmutableList(),
                            numberOfMembers = space.spaceRoom.numJoinedMembers,
                        )
                    }
                }
            }

            item {
                HorizontalDivider()
            }

            itemsIndexed(
                items = state.spaceRooms,
                key = { _, spaceRoom -> spaceRoom.roomId }
            ) { index, spaceRoom ->
                val isInvitation = spaceRoom.state == CurrentUserMembership.INVITED
                SpaceRoomItemView(
                    spaceRoom = spaceRoom,
                    showUnreadIndicator = isInvitation && spaceRoom.roomId !in state.seenSpaceInvites,
                    hideAvatars = isInvitation && state.hideInvitesAvatar,
                    onClick = {
                        onSpaceClick(spaceRoom.roomId)
                    },
                    onLongClick = {
                        // TODO
                    },
                )
                if (index != state.spaceRooms.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 * Ref: https://www.figma.com/design/pDlJZGBsri47FNTXMnEdXB/Compound-Android-Templates?node-id=1763-74215&t=9IGKMXHDfTGAqzQK-4
 */
@Composable
private fun EmptySpaceHomeView(
    onCreateSpaceClick: () -> Unit,
    onExploreClick: () -> Unit,
    canExploreSpaces: Boolean,
    modifier: Modifier = Modifier,
) {
    HeaderFooterPage(
        modifier = modifier,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 16.dp, start = 40.dp, end = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BigIcon(
                    style = BigIcon.Style.Default(CompoundIcons.SpaceSolid())
                )
                Text(
                    text = stringResource(CommonStrings.screen_space_list_empty_state_title),
                    style = ElementTheme.typography.fontHeadingMdBold,
                    color = ElementTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
            }
        },
        footer = {
            ButtonColumnMolecule {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(CommonStrings.action_create_space),
                    onClick = onCreateSpaceClick,
                )
                if (canExploreSpaces) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(CommonStrings.action_explore_public_spaces),
                        onClick = onExploreClick,
                    )
                }
            }
        }
    )
}

@PreviewsDayNight
@Composable
internal fun HomeSpacesViewPreview(
    @PreviewParameter(HomeSpacesStatePreviewParam::class) state: HomeSpacesState,
) = ElementPreview {
    HomeSpacesView(
        state = state,
        lazyListState = rememberLazyListState(),
        onSpaceClick = {},
        onCreateSpaceClick = {},
        onExploreClick = {},
        contentPadding = PaddingValues(bottom = 112.dp),
    )
}
