/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.components.receipt.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ModalBottomSheet
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.MatrixUserRow
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadReceiptBottomSheet(
    state: ReadReceiptBottomSheetState,
    onUserDataClick: (UserId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isVisible = state.selectedEvent != null

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
    )
    val coroutineScope = rememberCoroutineScope()
    if (isVisible) {
        ModalBottomSheet(
            modifier = modifier,
//            modifier = modifier.navigationBarsPadding() - FIXME after https://issuetracker.google.com/issues/275849044
//                    .imePadding()
            sheetState = sheetState,
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    state.eventSink(ReadReceiptBottomSheetEvent.Dismiss)
                }
            },
            scrollable = false,
        ) {
            ReadReceiptBottomSheetContent(
                state = state,
                onUserDataClick = {
                    coroutineScope.launch {
                        sheetState.hide()
                        state.eventSink(ReadReceiptBottomSheetEvent.Dismiss)
                        onUserDataClick.invoke(it)
                    }
                },
            )
            // FIXME remove after https://issuetracker.google.com/issues/275849044
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ReadReceiptBottomSheetContent(
    state: ReadReceiptBottomSheetState,
    onUserDataClick: (UserId) -> Unit,
) {
    LazyColumn {
        item {
            ListItem(
                content = {
                    Text(text = stringResource(id = CommonStrings.common_seen_by))
                }
            )
        }
        items(
            items = state.selectedEvent?.readReceiptState?.receipts.orEmpty()
        ) {
            val userId = UserId(it.avatarData.id)
            MatrixUserRow(
                modifier = Modifier.clickable { onUserDataClick(userId) },
                matrixUser = MatrixUser(
                    userId = userId,
                    displayName = it.avatarData.name,
                    avatarUrl = it.avatarData.url,
                ),
                avatarSize = AvatarSize.ReadReceiptList,
                trailingContent = {
                    Text(
                        text = it.formattedDate,
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textSecondary,
                    )
                }
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ReadReceiptBottomSheetPreview(@PreviewParameter(ReadReceiptBottomSheetStatePreviewParam::class) state: ReadReceiptBottomSheetState) {
    ElementPreview(fillMaxSize = true) {
        ReadReceiptBottomSheet(
            state = state,
            onUserDataClick = {},
        )
    }
}
