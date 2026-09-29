/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.appnav.room.joined

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.libraries.designsystem.components.button.BackButton
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.CircularProgressIndicator
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Scaffold
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TopAppBar
import cloud.orelit.communicationplatform.libraries.designsystem.utils.DelayedVisibility
import cloud.orelit.communicationplatform.libraries.matrix.ui.room.LoadingRoomState
import cloud.orelit.communicationplatform.libraries.matrix.ui.room.LoadingRoomStatePreviewParam
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
fun LoadingRoomNodeView(
    state: LoadingRoomState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            LoadingRoomTopBar(onBackClick)
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state is LoadingRoomState.Error) {
                    Text(
                        text = stringResource(id = CommonStrings.error_unknown),
                        color = ElementTheme.colors.textSecondary,
                        style = ElementTheme.typography.fontBodyMdRegular,
                    )
                } else {
                    DelayedVisibility {
                        CircularProgressIndicator()
                    }
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoadingRoomTopBar(
    onBackClick: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            BackButton(onClick = onBackClick)
        },
        title = {
        },
    )
}

@PreviewsDayNight
@Composable
internal fun LoadingRoomNodeViewPreview(@PreviewParameter(LoadingRoomStatePreviewParam::class) state: LoadingRoomState) = ElementPreview {
    LoadingRoomNodeView(
        state = state,
        onBackClick = {}
    )
}
