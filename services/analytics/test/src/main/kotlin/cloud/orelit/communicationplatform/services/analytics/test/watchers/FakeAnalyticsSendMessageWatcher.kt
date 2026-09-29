/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.services.analytics.test.watchers

import cloud.orelit.communicationplatform.services.analytics.api.watchers.AnalyticsSendMessageWatcher

class FakeAnalyticsSendMessageWatcher(
    private val startLambda: () -> Unit = {},
    private val stopLambda: () -> Unit = {},
) : AnalyticsSendMessageWatcher {
    override fun start() = startLambda()
    override fun stop() = stopLambda()
}
