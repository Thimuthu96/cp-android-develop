/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomdirectory.impl.root

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.roomdirectory.api.RoomDescription
import cloud.orelit.communicationplatform.features.roomdirectory.impl.R
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.Avatar
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarType
import cloud.orelit.communicationplatform.libraries.designsystem.components.button.BackButton
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.CircularProgressIndicator
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.FilledTextField
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.IconButton
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Scaffold
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TopAppBar
import cloud.orelit.communicationplatform.libraries.designsystem.utils.lazyColumnContentPadding
import cloud.orelit.communicationplatform.libraries.designsystem.utils.scaffoldScrollableContentInsets
import cloud.orelit.communicationplatform.libraries.testtags.TestTags
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@Composable
fun RoomDirectoryView(
    state: RoomDirectoryState,
    onResultClick: (RoomDescription) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            RoomDirectoryTopBar(onBackClick = onBackClick)
        },
        content = { padding ->
            RoomDirectoryContent(
                state = state,
                onResultClick = onResultClick,
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
            )
        },
        contentWindowInsets = scaffoldScrollableContentInsets,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomDirectoryTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            BackButton(onClick = onBackClick)
        },
        titleStr = stringResource(id = R.string.screen_room_directory_search_title),
    )
}

@Composable
private fun RoomDirectoryContent(
    state: RoomDirectoryState,
    onResultClick: (RoomDescription) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SearchTextField(
            query = state.query,
            onQueryChange = { state.eventSink(RoomDirectoryEvent.Search(it)) },
            placeholder = stringResource(id = CommonStrings.action_search),
            modifier = Modifier.fillMaxWidth(),
        )
        RoomDirectoryRoomList(
            roomDescriptions = state.roomDescriptions,
            displayLoadMoreIndicator = state.displayLoadMoreIndicator,
            displayEmptyState = state.displayEmptyState,
            onResultClick = onResultClick,
            onReachedLoadMore = { state.eventSink(RoomDirectoryEvent.LoadMore) },
        )
    }
}

@Composable
private fun RoomDirectoryRoomList(
    roomDescriptions: ImmutableList<RoomDescription>,
    displayLoadMoreIndicator: Boolean,
    displayEmptyState: Boolean,
    onResultClick: (RoomDescription) -> Unit,
    onReachedLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = lazyColumnContentPadding,
    ) {
        items(roomDescriptions) { roomDescription ->
            RoomDirectoryRoomRow(
                roomDescription = roomDescription,
                onClick = {
                    onResultClick(roomDescription)
                },
            )
        }
        if (displayEmptyState) {
            item {
                Text(
                    text = stringResource(id = CommonStrings.common_no_results),
                    style = ElementTheme.typography.fontBodyLgRegular,
                    color = ElementTheme.colors.textSecondary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        if (displayLoadMoreIndicator) {
            item {
                LoadMoreIndicator(modifier = Modifier.fillMaxWidth())
                LaunchedEffect(onReachedLoadMore) {
                    onReachedLoadMore()
                }
            }
        }
    }
}

@Composable
private fun LoadMoreIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            strokeWidth = 2.dp,
        )
    }
}

@Composable
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    colors: TextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        unfocusedPlaceholderColor = ElementTheme.colors.textSecondary,
        focusedPlaceholderColor = ElementTheme.colors.textSecondary,
        focusedTextColor = ElementTheme.colors.textPrimary,
        unfocusedTextColor = ElementTheme.colors.textPrimary,
        focusedIndicatorColor = ElementTheme.colors.borderInteractiveSecondary,
        unfocusedIndicatorColor = ElementTheme.colors.borderInteractiveSecondary,
    ),
) {
    val focusManager = LocalFocusManager.current
    FilledTextField(
        modifier = modifier.testTag(TestTags.searchTextField.value),
        textStyle = ElementTheme.typography.fontBodyLgRegular,
        singleLine = true,
        value = query,
        onValueChange = onQueryChange,
        keyboardActions = KeyboardActions(
            onSearch = {
                focusManager.clearFocus()
            }
        ),
        colors = colors,
        placeholder = { Text(placeholder) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onQueryChange("")
                    }
                ) {
                    Icon(
                        imageVector = CompoundIcons.Close(),
                        contentDescription = stringResource(CommonStrings.action_clear),
                    )
                }
            } else {
                Icon(
                    imageVector = CompoundIcons.Search(),
                    contentDescription = stringResource(CommonStrings.action_search),
                )
            }
        },
    )
}

@Composable
private fun RoomDirectoryRoomRow(
    roomDescription: RoomDescription,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                top = 12.dp,
                bottom = 12.dp,
                start = 16.dp,
            )
            .height(IntrinsicSize.Min),
    ) {
        Avatar(
            avatarData = roomDescription.avatarData(AvatarSize.RoomDirectoryItem),
            avatarType = AvatarType.Room(),
            modifier = Modifier.align(Alignment.CenterVertically),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = roomDescription.computedName,
                maxLines = 1,
                style = ElementTheme.typography.fontBodyLgRegular,
                color = ElementTheme.colors.textPrimary,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = roomDescription.computedDescription,
                maxLines = 1,
                style = ElementTheme.typography.fontBodyMdRegular,
                color = ElementTheme.colors.textSecondary,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun RoomDirectoryViewPreview(@PreviewParameter(RoomDirectoryStatePreviewParam::class) state: RoomDirectoryState) = ElementPreview {
    RoomDirectoryView(
        state = state,
        onResultClick = {},
        onBackClick = {},
    )
}
