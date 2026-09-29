/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.media

import cloud.orelit.communicationplatform.libraries.core.coroutine.CoroutineDispatchers
import cloud.orelit.communicationplatform.libraries.core.extensions.mapFailure
import cloud.orelit.communicationplatform.libraries.core.extensions.runCatchingExceptions
import cloud.orelit.communicationplatform.libraries.core.mimetype.MimeTypes.ensureDefaultSubtype
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MatrixMediaLoader
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaFile
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaSource
import cloud.orelit.communicationplatform.libraries.matrix.impl.exception.mapClientException
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.use
import java.io.File
import org.matrix.rustcomponents.sdk.MediaSource as RustMediaSource

class RustMediaLoader(
    private val baseCacheDirectory: File,
    dispatchers: CoroutineDispatchers,
    private val innerClient: Client,
) : MatrixMediaLoader {
    private val mediaDispatcher = dispatchers.io.limitedParallelism(32)
    private val cacheDirectory
        get() = File(baseCacheDirectory, "temp/media").apply {
            if (!exists()) mkdirs() // Must always ensure that this directory exists because "Clear cache" does not restart an app's process.
        }

    override suspend fun loadMediaContent(source: MediaSource): Result<ByteArray> =
        withContext(mediaDispatcher) {
            runCatchingExceptions {
                source.toRustMediaSource().use { source ->
                    innerClient.getMediaContent(source)
                }
            }.mapFailure { it.mapClientException() }
        }

    override suspend fun loadMediaThumbnail(
        source: MediaSource,
        width: Long,
        height: Long
    ): Result<ByteArray> =
        withContext(mediaDispatcher) {
            runCatchingExceptions {
                source.toRustMediaSource().use { mediaSource ->
                    innerClient.getMediaThumbnail(
                        mediaSource = mediaSource,
                        width = width.toULong(),
                        height = height.toULong()
                    )
                }
            }.mapFailure { it.mapClientException() }
        }

    override suspend fun downloadMediaFile(
        source: MediaSource,
        mimeType: String?,
        filename: String?,
        useCache: Boolean,
    ): Result<MediaFile> =
        withContext(mediaDispatcher) {
            runCatchingExceptions {
                source.toRustMediaSource().use { mediaSource ->
                    val mediaFile = innerClient.getMediaFile(
                        mediaSource = mediaSource,
                        filename = filename,
                        // Fallback to a default mime type based on the main type, so that the SDK can create a file with the correct extension.
                        mimeType = mimeType.ensureDefaultSubtype(),
                        useCache = useCache,
                        tempDir = cacheDirectory.path,
                    )
                    RustMediaFile(mediaFile)
                }
            }
        }

    private fun MediaSource.toRustMediaSource(): RustMediaSource {
        val json = this.json
        return if (json != null) {
            RustMediaSource.fromJson(json)
        } else {
            RustMediaSource.fromUrl(safeUrl)
        }
    }
}
