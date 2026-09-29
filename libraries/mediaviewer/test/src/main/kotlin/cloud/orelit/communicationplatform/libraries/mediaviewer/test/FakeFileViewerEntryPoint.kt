/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.test

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.FileViewerEntryPoint
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeFileViewerEntryPoint : FileViewerEntryPoint {
    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: FileViewerEntryPoint.Params,
        callback: FileViewerEntryPoint.Callback,
    ): Node = lambdaError()
}
