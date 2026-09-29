/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.test.analytics

import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsSdkManager
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsSdkSpan
import cloud.orelit.communicationplatform.services.analytics.api.NoopAnalyticsSdkSpan
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeAnalyticsSdkManager(
    private val enableSdkAnalyticsLambda: ((Boolean) -> Unit) = { lambdaError() },
) : AnalyticsSdkManager {
    override fun enableSdkAnalytics(enabled: Boolean) {
        enableSdkAnalyticsLambda(enabled)
    }

    override fun startSpan(name: String, parentTraceId: String?): AnalyticsSdkSpan = NoopAnalyticsSdkSpan
    override fun bridge(parentTraceId: String?): AnalyticsSdkSpan = NoopAnalyticsSdkSpan
}
