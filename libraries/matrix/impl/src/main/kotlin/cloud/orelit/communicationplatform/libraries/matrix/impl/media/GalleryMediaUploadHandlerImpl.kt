/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.media

import cloud.orelit.communicationplatform.libraries.androidutils.file.safeDelete
import cloud.orelit.communicationplatform.libraries.core.extensions.runCatchingExceptions
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaUploadHandler
import org.matrix.rustcomponents.sdk.SendGalleryJoinHandle
import java.io.File

class GalleryMediaUploadHandlerImpl(
    private val filesToUpload: List<File>,
    private val sendGalleryJoinHandle: SendGalleryJoinHandle,
) : MediaUploadHandler {
    override suspend fun await(): Result<Unit> =
        runCatchingExceptions {
            sendGalleryJoinHandle.join()
        }
            .also { cleanUpFiles() }

    override fun cancel() {
        sendGalleryJoinHandle.cancel()
        cleanUpFiles()
    }

    private fun cleanUpFiles() {
        filesToUpload.forEach { file -> file.safeDelete() }
    }
}
