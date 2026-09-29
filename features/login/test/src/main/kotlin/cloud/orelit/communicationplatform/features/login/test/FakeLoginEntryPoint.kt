/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.test

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import cloud.orelit.communicationplatform.features.login.api.LoginEntryPoint
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeLoginEntryPoint(
    private val createNodeResult: (BuildContext, LoginEntryPoint.Params) -> Node = { _, _ -> lambdaError() },
) : LoginEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: LoginEntryPoint.Params,
        callback: LoginEntryPoint.Callback,
    ): Node = createNodeResult(buildContext, params)
}
