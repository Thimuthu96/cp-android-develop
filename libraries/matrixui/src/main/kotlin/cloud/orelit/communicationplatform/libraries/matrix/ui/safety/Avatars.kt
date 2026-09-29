/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.ui.safety

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import cloud.orelit.communicationplatform.libraries.core.coroutine.mapState
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient

@Composable
fun MatrixClient.rememberHideInvitesAvatar(): State<Boolean> {
    return remember {
        mediaPreviewService
            .mediaPreviewConfigFlow
            .mapState { config -> config.hideInviteAvatar }
    }.collectAsState()
}
