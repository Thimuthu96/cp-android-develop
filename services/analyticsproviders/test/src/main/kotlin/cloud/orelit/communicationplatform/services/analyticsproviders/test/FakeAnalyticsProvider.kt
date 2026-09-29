/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.services.analyticsproviders.test

import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.SuperProperties
import im.vector.app.features.analytics.plan.UserProperties
import cloud.orelit.communicationplatform.services.analyticsproviders.api.AnalyticsProvider
import cloud.orelit.communicationplatform.services.analyticsproviders.api.AnalyticsTransaction
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeAnalyticsProvider(
    override val name: String = "FakeAnalyticsProvider",
    private val initLambda: () -> Unit = { lambdaError() },
    private val stopLambda: () -> Unit = { lambdaError() },
    private val captureLambda: (VectorAnalyticsEvent) -> Unit = { lambdaError() },
    private val screenLambda: (VectorAnalyticsScreen) -> Unit = { lambdaError() },
    private val updateUserPropertiesLambda: (UserProperties) -> Unit = { lambdaError() },
    private val updateSuperPropertiesLambda: (SuperProperties) -> Unit = { lambdaError() },
    private val trackErrorLambda: (Throwable) -> Unit = { lambdaError() }
) : AnalyticsProvider {
    override fun init() = initLambda()
    override fun stop() = stopLambda()
    override fun capture(event: VectorAnalyticsEvent) = captureLambda(event)
    override fun screen(screen: VectorAnalyticsScreen) = screenLambda(screen)
    override fun updateUserProperties(userProperties: UserProperties) = updateUserPropertiesLambda(userProperties)
    override fun trackError(throwable: Throwable) = trackErrorLambda(throwable)
    override fun updateSuperProperties(updatedProperties: SuperProperties) = updateSuperPropertiesLambda(updatedProperties)
    override fun startTransaction(name: String, operation: String?, description: String?): AnalyticsTransaction? = null
}
