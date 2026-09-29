/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.invite.api.declineandblock

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import cloud.orelit.communicationplatform.features.invite.api.InviteData
import cloud.orelit.communicationplatform.libraries.architecture.FeatureEntryPoint

interface DeclineInviteAndBlockEntryPoint : FeatureEntryPoint {
    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        inviteData: InviteData,
        callback: Callback,
    ): Node

    interface Callback : Plugin {
        fun onDeclineSuccess()
    }
}
