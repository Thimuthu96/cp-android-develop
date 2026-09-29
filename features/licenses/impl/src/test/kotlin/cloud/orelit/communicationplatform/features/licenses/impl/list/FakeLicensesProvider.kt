/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.licenses.impl.list

import cloud.orelit.communicationplatform.features.licenses.impl.LicensesProvider
import cloud.orelit.communicationplatform.features.licenses.impl.model.DependencyLicenseItem
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeLicensesProvider(
    private val provideResult: () -> List<DependencyLicenseItem> = { lambdaError() }
) : LicensesProvider {
    override suspend fun provides(): List<DependencyLicenseItem> {
        return provideResult()
    }
}
