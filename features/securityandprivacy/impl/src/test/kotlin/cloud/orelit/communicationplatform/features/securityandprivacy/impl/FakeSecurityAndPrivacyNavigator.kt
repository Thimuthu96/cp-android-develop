/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.securityandprivacy.impl

import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeSecurityAndPrivacyNavigator(
    private val onDoneLambda: () -> Unit = { lambdaError() },
    private val openEditRoomAddressLambda: () -> Unit = { lambdaError() },
    private val closeEditRoomAddressLambda: () -> Unit = { lambdaError() },
    private val openManageAuthorizedSpacesLambda: () -> Unit = { lambdaError() },
    private val closeManageAuthorizedSpacesLambda: () -> Unit = { lambdaError() },
) : SecurityAndPrivacyNavigator {
    override fun onDone() {
        onDoneLambda()
    }

    override fun openEditRoomAddress() {
        openEditRoomAddressLambda()
    }

    override fun closeEditRoomAddress() {
        closeEditRoomAddressLambda()
    }

    override fun openManageAuthorizedSpaces() {
        openManageAuthorizedSpacesLambda()
    }

    override fun closeManageAuthorizedSpaces() {
        closeManageAuthorizedSpacesLambda()
    }
}
