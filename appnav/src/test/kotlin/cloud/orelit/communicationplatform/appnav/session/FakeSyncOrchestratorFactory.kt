/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.appnav.session

import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.CoroutineScope

class FakeSyncOrchestratorFactory : SyncOrchestrator.Factory {
    override fun create(
        matrixClient: MatrixClient,
        sessionCoroutineScope: CoroutineScope,
    ): SyncOrchestrator = lambdaError()
}
