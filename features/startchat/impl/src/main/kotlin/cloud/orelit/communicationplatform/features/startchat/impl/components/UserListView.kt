/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.startchat.impl.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.features.startchat.impl.userlist.UserListEvent
import cloud.orelit.communicationplatform.features.startchat.impl.userlist.UserListState
import cloud.orelit.communicationplatform.features.startchat.impl.userlist.UserListStatePreviewParam
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.HorizontalDivider
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListSectionHeader
import cloud.orelit.communicationplatform.libraries.designsystem.utils.lazyColumnContentPadding
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.CheckableUserRow
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.CheckableUserRowData
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.SelectedUsersRowList
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getAvatarData
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getBestName
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
fun UserListView(
    state: UserListState,
    onSelectUser: (MatrixUser) -> Unit,
    onDeselectUser: (MatrixUser) -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
) {
    Column(
        modifier = modifier,
    ) {
        val contentPaddingValues = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues()
        SearchUserBar(
            modifier = Modifier.fillMaxWidth()
                .padding(contentPaddingValues),
            queryState = state.searchQuery,
            resultState = state.searchResults,
            selectedUsers = state.selectedUsers,
            active = state.isSearchActive,
            showLoader = state.showSearchLoader,
            isMultiSelectionEnable = state.isMultiSelectionEnabled,
            showBackButton = showBackButton,
            onActiveChange = { state.eventSink(UserListEvent.OnSearchActiveChanged(it)) },
            onUserSelect = {
                state.eventSink(UserListEvent.AddToSelection(it))
                onSelectUser(it)
            },
            onUserDeselect = {
                state.eventSink(UserListEvent.RemoveFromSelection(it))
                onDeselectUser(it)
            },
        )

        if (state.isMultiSelectionEnabled && !state.isSearchActive && state.selectedUsers.isNotEmpty()) {
            SelectedUsersRowList(
                contentPadding = contentPaddingValues + PaddingValues(16.dp),
                selectedUsers = state.selectedUsers,
                autoScroll = true,
                onUserRemove = {
                    state.eventSink(UserListEvent.RemoveFromSelection(it))
                    onDeselectUser(it)
                },
            )
        }
        if (!state.isSearchActive && state.recentDirectRooms.isNotEmpty()) {
            LazyColumn(
                contentPadding = lazyColumnContentPadding,
            ) {
                item {
                    ListSectionHeader(
                        title = stringResource(id = CommonStrings.common_suggestions),
                        hasDivider = false,
                    )
                }
                state.recentDirectRooms.forEachIndexed { index, recentDirectRoom ->
                    item {
                        val isSelected = state.selectedUsers.any {
                            recentDirectRoom.matrixUser.userId == it.userId
                        }
                        CheckableUserRow(
                            checked = isSelected,
                            onCheckedChange = {
                                if (isSelected) {
                                    state.eventSink(UserListEvent.RemoveFromSelection(recentDirectRoom.matrixUser))
                                    onDeselectUser(recentDirectRoom.matrixUser)
                                } else {
                                    state.eventSink(UserListEvent.AddToSelection(recentDirectRoom.matrixUser))
                                    onSelectUser(recentDirectRoom.matrixUser)
                                }
                            },
                            data = CheckableUserRowData.Resolved(
                                avatarData = recentDirectRoom.matrixUser.getAvatarData(AvatarSize.UserListItem),
                                name = recentDirectRoom.matrixUser.getBestName(),
                                subtext = recentDirectRoom.matrixUser.userId.value,
                            ),
                        )
                        if (index < state.recentDirectRooms.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun UserListViewPreview(@PreviewParameter(UserListStatePreviewParam::class) state: UserListState) = ElementPreview {
    UserListView(
        state = state,
        onSelectUser = {},
        onDeselectUser = {},
    )
}
