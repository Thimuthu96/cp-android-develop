/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.lockscreen.test

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import cloud.orelit.communicationplatform.features.lockscreen.api.DeviceUnlockEntryPoint
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeDeviceUnlockEntryPoint : DeviceUnlockEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
    ): Node = lambdaError()

    override fun requestUnlock(callback: DeviceUnlockEntryPoint.Callback) = lambdaError()
}
