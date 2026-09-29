/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.blockedusers

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import cloud.orelit.communicationplatform.features.preferences.impl.R
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.designsystem.components.async.AsyncIndicator
import cloud.orelit.communicationplatform.libraries.designsystem.components.async.AsyncIndicatorHost
import cloud.orelit.communicationplatform.libraries.designsystem.components.async.rememberAsyncIndicatorState
import cloud.orelit.communicationplatform.libraries.designsystem.components.button.BackButton
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.ConfirmationDialog
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Scaffold
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TopAppBar
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarHost
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.MatrixUserRow
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsersView(
    state: BlockedUsersState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)
    Box(modifier = modifier) {
        Scaffold(
            topBar = {
                TopAppBar(
                    titleStr = stringResource(CommonStrings.common_blocked_users),
                    navigationIcon = {
                        BackButton(onClick = onBackClick)
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding)
            ) {
                items(state.blockedUsers) { matrixUser ->
                    BlockedUserItem(
                        matrixUser = matrixUser,
                        onClick = { state.eventSink(BlockedUsersEvent.Unblock(it)) },
                        onLongClick = { state.eventSink(BlockedUsersEvent.CopyToClipboard(it)) },
                    )
                }
            }
        }

        val asyncIndicatorState = rememberAsyncIndicatorState()
        AsyncIndicatorHost(modifier = Modifier.statusBarsPadding(), state = asyncIndicatorState)

        when (state.unblockUserAction) {
            is AsyncAction.Loading -> {
                LaunchedEffect(state.unblockUserAction) {
                    asyncIndicatorState.enqueue {
                        AsyncIndicator.Loading(text = stringResource(R.string.screen_blocked_users_unblocking))
                    }
                }
            }
            is AsyncAction.Failure -> {
                LaunchedEffect(state.unblockUserAction) {
                    asyncIndicatorState.enqueue(durationMs = AsyncIndicator.DURATION_SHORT) {
                        AsyncIndicator.Failure(text = stringResource(CommonStrings.common_failed))
                    }
                }
            }
            is AsyncAction.Success -> {
                LaunchedEffect(state.unblockUserAction) {
                    asyncIndicatorState.clear()
                }
            }
            is AsyncAction.Confirming -> {
                ConfirmationDialog(
                    title = stringResource(R.string.screen_blocked_users_unblock_alert_title),
                    content = stringResource(R.string.screen_blocked_users_unblock_alert_description),
                    submitText = stringResource(R.string.screen_blocked_users_unblock_alert_action),
                    onSubmitClick = { state.eventSink(BlockedUsersEvent.ConfirmUnblock) },
                    onDismiss = { state.eventSink(BlockedUsersEvent.Cancel) }
                )
            }
            else -> Unit
        }
    }
}

@Composable
private fun BlockedUserItem(
    matrixUser: MatrixUser,
    onClick: (UserId) -> Unit,
    onLongClick: (UserId) -> Unit,
) {
    MatrixUserRow(
        modifier = Modifier.combinedClickable(
            onClick = { onClick(matrixUser.userId) },
            onLongClick = { onLongClick(matrixUser.userId) },
            onLongClickLabel = stringResource(CommonStrings.action_copy),
        ),
        matrixUser = matrixUser,
    )
}

@PreviewsDayNight
@Composable
internal fun BlockedUsersViewPreview(@PreviewParameter(BlockedUsersStatePreviewParam::class) state: BlockedUsersState) {
    ElementPreview {
        BlockedUsersView(
            state = state,
            onBackClick = {}
        )
    }
}
