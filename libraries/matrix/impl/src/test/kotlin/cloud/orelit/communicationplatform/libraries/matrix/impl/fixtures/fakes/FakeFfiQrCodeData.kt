/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.fixtures.fakes

import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.QrCodeData

class FakeFfiQrCodeData(
    private val serverNameResult: () -> String? = { lambdaError() },
    private val toBytesResult: () -> ByteArray = { lambdaError() },
) : QrCodeData(NoHandle) {
    override fun serverName(): String? {
        return serverNameResult()
    }

    override fun toBytes(): ByteArray {
        return toBytesResult()
    }
}
