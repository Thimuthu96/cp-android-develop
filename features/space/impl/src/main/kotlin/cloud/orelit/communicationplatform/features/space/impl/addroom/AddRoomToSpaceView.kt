/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.space.impl.addroom

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.features.space.impl.R
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.designsystem.components.async.AsyncActionView
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarType
import cloud.orelit.communicationplatform.libraries.designsystem.components.button.BackButton
import cloud.orelit.communicationplatform.libraries.designsystem.components.list.AvatarListItem
import cloud.orelit.communicationplatform.libraries.designsystem.components.list.ListItemContent
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListSectionHeader
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Scaffold
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.SearchBar
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TextButton
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TopAppBar
import cloud.orelit.communicationplatform.libraries.designsystem.utils.OnVisibleRangeChangeEffect
import cloud.orelit.communicationplatform.libraries.designsystem.utils.lazyColumnContentPadding
import cloud.orelit.communicationplatform.libraries.designsystem.utils.scaffoldScrollableContentInsets
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.SelectedRoom
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.SelectRoomInfo
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getAvatarData
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoomToSpaceView(
    state: AddRoomToSpaceState,
    onBackClick: () -> Unit,
    onAddRoom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val updatedOnAddRoom by rememberUpdatedState(onAddRoom)
    fun onRoomToggled(room: SelectRoomInfo) {
        state.eventSink(AddRoomToSpaceEvent.ToggleRoom(room))
    }

    fun onBack() {
        if (state.isSearchActive) {
            state.eventSink(AddRoomToSpaceEvent.OnSearchActiveChanged(false))
        } else {
            state.eventSink(AddRoomToSpaceEvent.Dismiss)
            onBackClick()
        }
    }

    BackHandler(onBack = ::onBack)

    // Navigate back on success
    LaunchedEffect(state.saveAction) {
        if (state.saveAction is AsyncAction.Success) {
            updatedOnAddRoom()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                titleStr = stringResource(CommonStrings.action_add_existing_rooms),
                navigationIcon = {
                    BackButton(onClick = ::onBack)
                },
                actions = {
                    TextButton(
                        text = stringResource(CommonStrings.action_save),
                        enabled = state.canSave,
                        onClick = { state.eventSink(AddRoomToSpaceEvent.Save) }
                    )
                }
            )
        },
        contentWindowInsets = scaffoldScrollableContentInsets,
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        WindowInsets.safeDrawing
                            .only(WindowInsetsSides.Horizontal)
                            .asPaddingValues()
                    ),
                placeHolderTitle = stringResource(CommonStrings.action_search),
                queryState = state.searchQuery,
                active = state.isSearchActive,
                onActiveChange = { state.eventSink(AddRoomToSpaceEvent.OnSearchActiveChanged(it)) },
                showBackButton = false,
                resultState = state.searchResults,
                contentPrefix = {
                    if (state.selectedRooms.isNotEmpty()) {
                        SelectedRoomsRow(
                            selectedRooms = state.selectedRooms,
                            onRemoveRoom = ::onRoomToggled,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                },
            ) { rooms ->
                val lazyListState = rememberLazyListState()
                OnVisibleRangeChangeEffect(lazyListState) { visibleRange ->
                    state.eventSink(AddRoomToSpaceEvent.UpdateSearchVisibleRange(visibleRange))
                }
                LazyColumn(
                    contentPadding = lazyColumnContentPadding,
                ) {
                    items(rooms, key = { it.roomId }) { roomInfo ->
                        RoomListItem(
                            roomInfo = roomInfo,
                            isSelected = state.selectedRooms.any { it.roomId == roomInfo.roomId },
                            onToggle = ::onRoomToggled
                        )
                    }
                }
            }

            if (!state.isSearchActive) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            WindowInsets.safeDrawing
                                .only(WindowInsetsSides.Horizontal)
                                .asPaddingValues()
                        )
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.screen_space_add_rooms_room_access_description),
                        color = ElementTheme.colors.textSecondary,
                        style = ElementTheme.typography.fontBodySmRegular,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    if (state.selectedRooms.isNotEmpty()) {
                        SelectedRoomsRow(
                            selectedRooms = state.selectedRooms,
                            onRemoveRoom = ::onRoomToggled,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }

                if (state.suggestions.isNotEmpty()) {
                    LazyColumn(
                        contentPadding = lazyColumnContentPadding,
                    ) {
                        item {
                            ListSectionHeader(
                                title = stringResource(id = CommonStrings.common_suggestions),
                                hasDivider = true,
                            )
                        }
                        items(state.suggestions, key = { it.roomId.value }) { roomInfo ->
                            RoomListItem(
                                roomInfo = roomInfo,
                                isSelected = state.selectedRooms.any { it.roomId == roomInfo.roomId },
                                onToggle = ::onRoomToggled
                            )
                        }
                    }
                }
            }
        }
    }
    SaveActionView(
        saveAction = state.saveAction,
        onRetry = { state.eventSink(AddRoomToSpaceEvent.Save) },
        onDismiss = { state.eventSink(AddRoomToSpaceEvent.ResetSaveAction) }
    )
}

@Composable
private fun SaveActionView(
    saveAction: AsyncAction<Unit>,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    AsyncActionView(
        async = saveAction,
        onRetry = onRetry,
        errorTitle = {
            stringResource(CommonStrings.common_something_went_wrong)
        },
        errorMessage = {
            stringResource(CommonStrings.error_network_or_server_issue)
        },
        onSuccess = { onDismiss() },
        onErrorDismiss = onDismiss,
    )
}

@Composable
private fun SelectedRoomsRow(
    selectedRooms: ImmutableList<SelectRoomInfo>,
    onRemoveRoom: (SelectRoomInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        items(selectedRooms, key = { it.roomId }) { roomInfo ->
            SelectedRoom(roomInfo = roomInfo, onRemoveRoom = { onRemoveRoom(roomInfo) })
        }
    }
}

@Composable
private fun RoomListItem(
    roomInfo: SelectRoomInfo,
    isSelected: Boolean,
    onToggle: (SelectRoomInfo) -> Unit,
) {
    AvatarListItem(
        avatarData = roomInfo.getAvatarData(size = AvatarSize.RoomSelectRoomListItem),
        avatarType = AvatarType.Room(
            heroes = roomInfo.heroes.map { user ->
                user.getAvatarData(size = AvatarSize.RoomSelectRoomListItem)
            }.toImmutableList(),
            isTombstoned = roomInfo.isTombstoned,
        ),
        headline = roomInfo.name ?: stringResource(id = CommonStrings.common_no_room_name),
        supportingText = roomInfo.canonicalAlias?.value,
        trailingContent = ListItemContent.Checkbox(checked = isSelected),
        onClick = { onToggle(roomInfo) },
    )
}

@PreviewsDayNight
@Composable
internal fun AddRoomToSpaceViewPreview(
    @PreviewParameter(AddRoomToSpaceStatePreviewParam::class) state: AddRoomToSpaceState
) = ElementPreview {
    AddRoomToSpaceView(
        state = state,
        onBackClick = {},
        onAddRoom = {},
    )
}
