/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.invite.impl.acceptdecline

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.features.invite.api.acceptdecline.AcceptDeclineInviteState
import cloud.orelit.communicationplatform.features.invite.api.acceptdecline.AcceptDeclineInviteView
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId

@ContributesBinding(SessionScope::class)
class DefaultAcceptDeclineInviteView : AcceptDeclineInviteView {
    @Composable
    override fun Render(
        state: AcceptDeclineInviteState,
        onAcceptInviteSuccess: (RoomId) -> Unit,
        onDeclineInviteSuccess: (RoomId) -> Unit,
        modifier: Modifier,
    ) {
        AcceptDeclineInviteView(
            state = state,
            onAcceptInviteSuccess = onAcceptInviteSuccess,
            onDeclineInviteSuccess = onDeclineInviteSuccess,
            modifier = modifier
        )
    }
}
