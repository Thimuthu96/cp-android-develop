/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl

import cloud.orelit.communicationplatform.libraries.core.extensions.mapFailure
import cloud.orelit.communicationplatform.libraries.core.extensions.runCatchingExceptions
import cloud.orelit.communicationplatform.libraries.matrix.api.TemporaryMatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.paths.SessionPaths
import cloud.orelit.communicationplatform.libraries.matrix.impl.exception.mapClientException
import org.matrix.rustcomponents.sdk.Client

class RustTemporaryMatrixClient(
    private val client: Client,
    private val paths: SessionPaths?,
) : TemporaryMatrixClient {
    override suspend fun getUrl(url: String): Result<ByteArray> = runCatchingExceptions {
        client.getUrl(url)
    }.mapFailure { it.mapClientException() }

    override val server: String? = client.server()
    override val homeserverUrl: String = client.homeserver()

    override fun close() {
        client.close()
        paths?.let {
            if (it.fileDirectory.exists()) {
                it.fileDirectory.deleteRecursively()
            }
            if (it.cacheDirectory.exists()) {
                it.cacheDirectory.deleteRecursively()
            }
        }
    }
}
