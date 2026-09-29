/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.push.impl.notifications

import cloud.orelit.communicationplatform.libraries.push.impl.db.PushRequest
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.model.ResolvedPushEvent
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeNotificationResultProcessor(
    private val emit: (Map<PushRequest, Result<ResolvedPushEvent>>) -> Unit = { lambdaError() },
    private val start: () -> Unit = { lambdaError() },
    private val stop: () -> Unit = { lambdaError() },
) : NotificationResultProcessor {
    override suspend fun emit(results: Map<PushRequest, Result<ResolvedPushEvent>>) {
        return emit.invoke(results)
    }

    override fun start() {
        start.invoke()
    }

    override fun stop() {
        stop.invoke()
    }
}
