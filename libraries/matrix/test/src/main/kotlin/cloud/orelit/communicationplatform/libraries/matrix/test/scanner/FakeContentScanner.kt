/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.test.scanner

import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaSource
import cloud.orelit.communicationplatform.libraries.matrix.api.scanner.ContentScanner

class FakeContentScanner(
    private val scan: suspend (mediaSource: MediaSource) -> Result<Boolean> = { Result.success(true) },
) : ContentScanner {
    override suspend fun scan(mediaSource: MediaSource): Result<Boolean> {
        return scan.invoke(mediaSource)
    }
}
