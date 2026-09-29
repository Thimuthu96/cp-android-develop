/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.linknewdevice

import cloud.orelit.communicationplatform.libraries.matrix.api.linknewdevice.ContinuationMessageSender
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import cloud.orelit.communicationplatform.tests.testutils.simulateLongTask

class FakeContinuationMessageSender(
    private val cancelResult: () -> Result<Unit> = { lambdaError() },
    private val confirmResult: () -> Result<Unit> = { lambdaError() },
) : ContinuationMessageSender {
    override suspend fun cancel(): Result<Unit> = simulateLongTask {
        cancelResult()
    }

    override suspend fun confirm(): Result<Unit> = simulateLongTask {
        confirmResult()
    }
}
