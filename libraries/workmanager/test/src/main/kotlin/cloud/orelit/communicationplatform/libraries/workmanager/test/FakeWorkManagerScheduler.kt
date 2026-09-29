/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.workmanager.test

import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.workmanager.api.WorkManagerRequestBuilder
import cloud.orelit.communicationplatform.libraries.workmanager.api.WorkManagerRequestType
import cloud.orelit.communicationplatform.libraries.workmanager.api.WorkManagerScheduler
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeWorkManagerScheduler(
    private val submitLambda: (WorkManagerRequestBuilder) -> Unit = { lambdaError() },
    private val hasPendingWorkLambda: (SessionId, WorkManagerRequestType) -> Boolean = { _, _ -> false },
    private val cancelLambda: (SessionId, WorkManagerRequestType?) -> Unit = { _, _ -> lambdaError() },
) : WorkManagerScheduler {
    override suspend fun submit(workManagerRequestBuilder: WorkManagerRequestBuilder) {
        submitLambda(workManagerRequestBuilder)
    }

    override fun hasPendingWork(sessionId: SessionId, requestType: WorkManagerRequestType): Boolean {
        return hasPendingWorkLambda(sessionId, requestType)
    }

    override fun cancel(sessionId: SessionId, requestType: WorkManagerRequestType?) {
        cancelLambda(sessionId, requestType)
    }
}
