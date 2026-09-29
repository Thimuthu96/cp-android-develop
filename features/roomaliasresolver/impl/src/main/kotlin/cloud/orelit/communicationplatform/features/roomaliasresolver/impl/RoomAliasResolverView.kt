/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomaliasresolver.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.atoms.PlaceholderAtom
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.atoms.RoomPreviewAliasAtom
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.organisms.RoomPreviewOrganism
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.pages.HeaderFooterPage
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.components.button.BackButton
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.ErrorDialog
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.RetryDialog
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.CircularProgressIndicator
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TopAppBar
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.room.alias.ResolvedRoomAlias
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
fun RoomAliasResolverView(
    state: RoomAliasResolverState,
    onBackClick: () -> Unit,
    onSuccess: (ResolvedRoomAlias) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        HeaderFooterPage(
            containerColor = Color.Transparent,
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 24.dp
            ),
            topBar = {
                RoomAliasResolverTopBar(onBackClick = onBackClick)
            },
            content = {
                RoomAliasResolverContent(roomAlias = state.roomAlias, isLoading = state.resolveState.isLoading())
            },
        )
        ResolvedRoomAliasView(
            resolvedRoomAlias = state.resolveState,
            onSuccess = onSuccess,
            onRetry = { state.eventSink(RoomAliasResolverEvent.Retry) },
            onDismissError = {
                state.eventSink(RoomAliasResolverEvent.DismissError)
                onBackClick()
            }
        )
    }
}

@Composable
private fun ResolvedRoomAliasView(
    resolvedRoomAlias: AsyncData<ResolvedRoomAlias>,
    onSuccess: (ResolvedRoomAlias) -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit,
) {
    when (resolvedRoomAlias) {
        is AsyncData.Success -> {
            val latestOnSuccess by rememberUpdatedState(onSuccess)
            LaunchedEffect(Unit) {
                latestOnSuccess(resolvedRoomAlias.data)
            }
        }
        is AsyncData.Failure -> {
            if (resolvedRoomAlias.error is RoomAliasResolverFailures.UnknownAlias) {
                ErrorDialog(
                    title = stringResource(id = R.string.screen_join_room_loading_alert_title),
                    content = stringResource(id = R.string.screen_room_alias_resolver_resolve_alias_failure),
                    onSubmit = onDismissError
                )
            } else {
                RetryDialog(
                    title = stringResource(id = R.string.screen_join_room_loading_alert_title),
                    content = stringResource(id = CommonStrings.error_network_or_server_issue),
                    onRetry = onRetry,
                    onDismiss = onDismissError
                )
            }
        }
        else -> Unit
    }
}

@Composable
private fun RoomAliasResolverContent(
    roomAlias: RoomAlias,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    RoomPreviewOrganism(
        modifier = modifier,
        avatar = {
            PlaceholderAtom(width = AvatarSize.RoomPreviewHeader.dp, height = AvatarSize.RoomPreviewHeader.dp)
        },
        title = {
            RoomPreviewAliasAtom(roomAlias.value)
        },
        subtitle = {
            if (isLoading) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomAliasResolverTopBar(
    onBackClick: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            BackButton(onClick = onBackClick)
        },
        title = {},
    )
}

@PreviewsDayNight
@Composable
internal fun RoomAliasResolverViewPreview(@PreviewParameter(RoomAliasResolverStatePreviewParam::class) state: RoomAliasResolverState) = ElementPreview {
    RoomAliasResolverView(
        state = state,
        onSuccess = { },
        onBackClick = { }
    )
}
