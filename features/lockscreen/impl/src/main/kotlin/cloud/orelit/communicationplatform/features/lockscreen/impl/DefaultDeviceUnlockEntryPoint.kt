/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.lockscreen.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.features.lockscreen.api.DeviceUnlockEntryPoint
import cloud.orelit.communicationplatform.features.lockscreen.impl.device.DeviceUnlockCallbackHolder
import cloud.orelit.communicationplatform.features.lockscreen.impl.device.DeviceUnlockNode
import cloud.orelit.communicationplatform.libraries.architecture.createNode

@ContributesBinding(AppScope::class)
class DefaultDeviceUnlockEntryPoint(
    private val deviceUnlockCallbackHolder: DeviceUnlockCallbackHolder,
) : DeviceUnlockEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
    ): Node {
        return parentNode.createNode<DeviceUnlockNode>(
            buildContext = buildContext,
        )
    }

    override fun requestUnlock(callback: DeviceUnlockEntryPoint.Callback) {
        deviceUnlockCallbackHolder.requestUnlock(callback)
    }
}
