/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.createroom.impl.configureroom

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.features.createroom.impl.R
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.Avatar
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarType
import cloud.orelit.communicationplatform.libraries.designsystem.components.list.ListItemContent
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListSectionHeader
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ModalBottomSheet
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.hide
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.spaces.SpaceRoom
import cloud.orelit.communicationplatform.libraries.previewutils.room.aSpaceRoom
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectParentSpaceOptions(
    spaces: ImmutableList<SpaceRoom>,
    selectedSpace: SpaceRoom?,
    onSelectSpace: (SpaceRoom?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var displaySelectSpaceBottomSheet by remember { mutableStateOf(false) }
    ConfigureRoomOptions(
        title = stringResource(CommonStrings.common_space),
        hasDivider = false,
        modifier = modifier
    ) {
        ListItem(
            content = {
                Text(
                    text = selectedSpace?.displayName
                        ?: stringResource(R.string.screen_create_room_space_selection_no_space_title),
                    maxLines = 1,
                    color = ElementTheme.colors.textPrimary
                )
            },
            supportingContent = selectedSpace?.canonicalAlias?.let { alias ->
                {
                    Text(text = alias.value, maxLines = 1)
                }
            },
            leadingContent = selectedSpace?.let {
                ListItemContent.Custom({
                    val avatarData = AvatarData(
                        id = selectedSpace.roomId.value,
                        name = selectedSpace.displayName,
                        url = selectedSpace.avatarUrl,
                        size = AvatarSize.SelectParentSpace,
                    )
                    Avatar(avatarData = avatarData, avatarType = AvatarType.Space())
                })
            },
            onClick = { displaySelectSpaceBottomSheet = true }
        )

        if (displaySelectSpaceBottomSheet) {
            val sheetState = rememberBottomSheetState(
                initialValue = SheetValue.Hidden,
                enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
            )
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    sheetState.hide(coroutineScope) {
                        displaySelectSpaceBottomSheet = false
                    }
                },
                scrollable = false,
            ) {
                SelectParentSpaceBottomSheet(
                    spaces = spaces,
                    selectedSpace = selectedSpace,
                ) {
                    sheetState.hide(coroutineScope) {
                        displaySelectSpaceBottomSheet = false
                    }
                    onSelectSpace(it)
                }
            }
        }
    }
}

@Composable
private fun SelectParentSpaceBottomSheet(
    spaces: ImmutableList<SpaceRoom>,
    selectedSpace: SpaceRoom?,
    onSelectSpace: (SpaceRoom?) -> Unit,
) {
    ListSectionHeader(
        title = stringResource(R.string.screen_create_room_space_selection_sheet_title),
        hasDivider = false
    )
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            ListItem(
                content = {
                    Text(
                        text = stringResource(R.string.screen_create_room_space_selection_no_space_option),
                        maxLines = 1
                    )
                },
                trailingContent = ListItemContent.RadioButton(
                    selected = selectedSpace == null
                ),
                onClick = { onSelectSpace(null) },
            )
        }
        for (space in spaces) {
            item {
                ListItem(
                    content = {
                        Text(
                            text = space.displayName,
                            maxLines = 1
                        )
                    },
                    supportingContent = space.canonicalAlias?.let { alias ->
                        {
                            Text(
                                text = alias.value,
                                maxLines = 1
                            )
                        }
                    },
                    leadingContent = ListItemContent.Custom({
                        val avatarData =
                            AvatarData(
                                id = space.roomId.value,
                                name = space.displayName,
                                url = space.avatarUrl,
                                size = AvatarSize.SelectParentSpace,
                            )
                        Avatar(
                            avatarData = avatarData,
                            avatarType = AvatarType.Space()
                        )
                    }),
                    trailingContent = ListItemContent.RadioButton(
                        selected = selectedSpace == space
                    ),
                    onClick = { onSelectSpace(space) },
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun SelectParentSpaceBottomSheetPreview() =
    ElementPreview {
        Column {
            SelectParentSpaceBottomSheet(
                spaces = persistentListOf(
                    aSpaceRoom(
                        canonicalAlias = RoomAlias(
                            "#a-room-alias:example.org"
                        )
                    ),
                    aSpaceRoom()
                ),
                selectedSpace = null,
            ) {}
        }
    }
