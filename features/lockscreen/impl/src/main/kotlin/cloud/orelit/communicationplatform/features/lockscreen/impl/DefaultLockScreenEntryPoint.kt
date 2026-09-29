/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.lockscreen.impl

import android.content.Context
import android.content.Intent
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.features.lockscreen.api.LockScreenEntryPoint
import cloud.orelit.communicationplatform.features.lockscreen.impl.unlock.activity.PinUnlockActivity
import cloud.orelit.communicationplatform.libraries.architecture.createNode

@ContributesBinding(AppScope::class)
class DefaultLockScreenEntryPoint : LockScreenEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        navTarget: LockScreenEntryPoint.Target,
        callback: LockScreenEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<LockScreenFlowNode>(
            buildContext = buildContext,
            plugins = listOf(
                LockScreenFlowNode.Inputs(
                    when (navTarget) {
                        LockScreenEntryPoint.Target.Setup -> LockScreenFlowNode.NavTarget.Setup
                        LockScreenEntryPoint.Target.Settings -> LockScreenFlowNode.NavTarget.Settings
                    }
                ),
                callback,
            )
        )
    }

    override fun pinUnlockIntent(context: Context): Intent {
        return PinUnlockActivity.newIntent(context)
    }
}
