/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.invitepeople.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.features.invitepeople.api.InvitePeopleRenderer
import cloud.orelit.communicationplatform.features.invitepeople.api.InvitePeopleState
import cloud.orelit.communicationplatform.libraries.di.SessionScope

@ContributesBinding(SessionScope::class)
class DefaultInvitePeopleRenderer : InvitePeopleRenderer {
    @Composable
    override fun Render(state: InvitePeopleState, modifier: Modifier) {
        if (state is DefaultInvitePeopleState) {
            InvitePeopleView(
                state = state,
                modifier = modifier
            )
        } else {
            error("Unsupported state type: ${state::javaClass}")
        }
    }
}
