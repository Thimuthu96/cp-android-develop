/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.logout.test

import cloud.orelit.communicationplatform.features.logout.api.LogoutUseCase
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import cloud.orelit.communicationplatform.tests.testutils.simulateLongTask

class FakeLogoutUseCase(
    var logoutLambda: (Boolean) -> Unit = { lambdaError() }
) : LogoutUseCase {
    override suspend fun logoutAll(ignoreSdkError: Boolean) = simulateLongTask {
        logoutLambda(ignoreSdkError)
    }
}
