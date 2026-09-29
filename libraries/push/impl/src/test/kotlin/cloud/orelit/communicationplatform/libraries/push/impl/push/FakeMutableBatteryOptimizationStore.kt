/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.push.impl.push

import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeMutableBatteryOptimizationStore(
    private val showBatteryOptimizationBannerResult: () -> Unit = { lambdaError() },
    private val onOptimizationBannerDismissedResult: () -> Unit = { lambdaError() },
    private val resetResult: () -> Unit = { lambdaError() },
) : MutableBatteryOptimizationStore {
    override suspend fun showBatteryOptimizationBanner() {
        showBatteryOptimizationBannerResult()
    }

    override suspend fun onOptimizationBannerDismissed() {
        onOptimizationBannerDismissedResult()
    }

    override suspend fun reset() {
        resetResult()
    }
}
