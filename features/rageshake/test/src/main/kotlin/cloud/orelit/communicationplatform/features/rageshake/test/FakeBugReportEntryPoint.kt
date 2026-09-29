/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.rageshake.test

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import cloud.orelit.communicationplatform.features.rageshake.api.bugreport.BugReportEntryPoint
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeBugReportEntryPoint(
    private val createNodeResult: (BuildContext, BugReportEntryPoint.Callback) -> Node = { _, _ -> lambdaError() },
) : BugReportEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: BugReportEntryPoint.Callback,
    ): Node = createNodeResult(buildContext, callback)
}
