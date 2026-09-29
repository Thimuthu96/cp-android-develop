/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.user.editprofile

import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeEditUserProfileNavigator(
    val closeLambda: () -> Unit = { lambdaError() }
) : EditUserProfileNavigator {
    override fun close() = closeLambda()
}
