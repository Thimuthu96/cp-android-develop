/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.test

import cloud.orelit.communicationplatform.libraries.matrix.api.TemporaryMatrixClient
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeTemporaryMatrixClient(
    override val server: String = A_SERVER_NAME,
    override val homeserverUrl: String = A_HOMESERVER_URL,
    private val getUrlResult: (String) -> Result<ByteArray> = { lambdaError() },
    private val closeLambda: () -> Unit = {},
) : TemporaryMatrixClient {
    override suspend fun getUrl(url: String): Result<ByteArray> = getUrlResult(url)
    override fun close() = closeLambda()
}
