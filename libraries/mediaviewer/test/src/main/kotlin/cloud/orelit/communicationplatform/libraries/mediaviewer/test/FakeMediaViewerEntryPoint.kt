/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.test

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.MediaViewerEntryPoint
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeMediaViewerEntryPoint : MediaViewerEntryPoint {
    override fun createParamsForAvatar(filename: String, avatarUrl: String) = lambdaError()

    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: MediaViewerEntryPoint.Params,
        callback: MediaViewerEntryPoint.Callback,
    ): Node = lambdaError()
}
