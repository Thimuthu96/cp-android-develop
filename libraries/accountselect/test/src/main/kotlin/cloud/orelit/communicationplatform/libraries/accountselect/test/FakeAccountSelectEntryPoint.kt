/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.accountselect.test

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import cloud.orelit.communicationplatform.libraries.accountselect.api.AccountSelectEntryPoint
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeAccountSelectEntryPoint(
    private val createNodeResult: (BuildContext, AccountSelectEntryPoint.Callback) -> Node = { _, _ -> lambdaError() },
) : AccountSelectEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: AccountSelectEntryPoint.Callback,
    ): Node = createNodeResult(buildContext, callback)
}
