/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.test.auth.qrlogin

import cloud.orelit.communicationplatform.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginDataFactory
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder

class FakeMatrixQrCodeLoginDataFactory(
    var parseQrCodeLoginDataResult: () -> Result<MatrixQrCodeLoginData> =
        lambdaRecorder<Result<MatrixQrCodeLoginData>> { Result.success(FakeMatrixQrCodeLoginData()) },
) : MatrixQrCodeLoginDataFactory {
    override fun parseQrCodeData(data: ByteArray): Result<MatrixQrCodeLoginData> {
        return parseQrCodeLoginDataResult()
    }
}

class FakeMatrixQrCodeLoginData(
    private val serverNameResult: () -> String? = { lambdaError() },
) : MatrixQrCodeLoginData {
    override fun serverName() = serverNameResult()
}
