/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.pushproviders.firebase

import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeRotateFirebaseSession(
    private val rotateWithResult: () -> Result<Unit> = { lambdaError() }
) : RotateFirebaseSession {
    override suspend fun invoke(): Result<Unit> {
        return rotateWithResult()
    }
}
