/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.linknewdevice.impl.screens.number

import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeEnterNumberNavigator(
    private val navigateToWrongNumberErrorLambda: () -> Unit = { lambdaError() },
) : EnterNumberNavigator {
    override fun navigateToWrongNumberError() {
        navigateToWrongNumberErrorLambda()
    }
}
