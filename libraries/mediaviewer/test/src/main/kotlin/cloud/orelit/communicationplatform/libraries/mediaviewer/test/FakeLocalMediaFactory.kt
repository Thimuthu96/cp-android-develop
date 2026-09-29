/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.test

import android.net.Uri
import cloud.orelit.communicationplatform.libraries.core.mimetype.MimeTypes
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaFile
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.MediaInfo
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.local.LocalMedia
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.local.LocalMediaFactory
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.util.FileExtensionExtractor
import cloud.orelit.communicationplatform.libraries.mediaviewer.test.util.FileExtensionExtractorWithoutValidation
import cloud.orelit.communicationplatform.libraries.mediaviewer.test.viewer.aLocalMedia

class FakeLocalMediaFactory(
    private val localMediaUri: Uri,
    private val fileExtensionExtractor: FileExtensionExtractor = FileExtensionExtractorWithoutValidation()
) : LocalMediaFactory {
    var fallbackMimeType: String = MimeTypes.OctetStream
    var fallbackName: String = "File name"
    var fallbackFileSize = "0B"

    override fun createFromMediaFile(mediaFile: MediaFile, mediaInfo: MediaInfo): LocalMedia {
        return aLocalMedia(uri = localMediaUri, mediaInfo = mediaInfo)
    }

    override fun createFromUri(
        uri: Uri,
        mimeType: String?,
        name: String?,
        formattedFileSize: String?
    ): LocalMedia {
        val safeName = name ?: fallbackName
        val mediaInfo = MediaInfo(
            filename = safeName,
            fileSize = null,
            caption = null,
            formattedCaption = null,
            mimeType = mimeType ?: fallbackMimeType,
            formattedFileSize = formattedFileSize ?: fallbackFileSize,
            fileExtension = fileExtensionExtractor.extractFromName(safeName),
            senderId = null,
            senderName = null,
            senderAvatar = null,
            dateSent = null,
            dateSentFull = null,
            waveform = null,
            duration = null,
        )
        return aLocalMedia(uri, mediaInfo)
    }
}
