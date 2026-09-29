/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.push.impl.test

import cloud.orelit.communicationplatform.libraries.push.impl.BuildConfig
import cloud.orelit.communicationplatform.libraries.push.impl.pushgateway.PushGatewayNotifyRequest
import cloud.orelit.communicationplatform.libraries.pushproviders.test.aSessionPushConfig
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultTestPushTest {
    @Test
    fun `test DefaultTestPush`() = runTest {
        val executeResult = lambdaRecorder<PushGatewayNotifyRequest.Params, Unit> { }
        val defaultTestPush = DefaultTestPush(
            pushGatewayNotifyRequest = FakePushGatewayNotifyRequest(
                executeResult = executeResult,
            )
        )
        val aConfig = aSessionPushConfig()
        defaultTestPush.execute(aConfig)
        executeResult.assertions()
            .isCalledOnce()
            .with(
                value(
                    PushGatewayNotifyRequest.Params(
                        url = aConfig.url,
                        appId = BuildConfig.PUSHER_APP_ID,
                        pushKey = aConfig.pushKey,
                        eventId = DefaultTestPush.TEST_EVENT_ID,
                        roomId = DefaultTestPush.TEST_ROOM_ID,
                    )
                )
            )
    }
}
