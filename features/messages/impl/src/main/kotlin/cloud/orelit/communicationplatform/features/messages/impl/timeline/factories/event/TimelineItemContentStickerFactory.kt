/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.event

import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemEventContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import cloud.orelit.communicationplatform.libraries.androidutils.filesize.FileSizeFormatter
import cloud.orelit.communicationplatform.libraries.core.mimetype.MimeTypes
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.StickerContent
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.util.FileExtensionExtractor

@Inject
class TimelineItemContentStickerFactory(
    private val fileSizeFormatter: FileSizeFormatter,
    private val fileExtensionExtractor: FileExtensionExtractor
) {
    private fun aspectRatioOf(width: Long?, height: Long?): Float? {
        val result = if (height != null && width != null) {
            width.toFloat() / height.toFloat()
        } else {
            null
        }

        return result?.takeIf { it.isFinite() }
    }

    fun create(content: StickerContent): TimelineItemEventContent {
        val aspectRatio = aspectRatioOf(content.info.width, content.info.height)

        return TimelineItemStickerContent(
            filename = content.filename,
            fileSize = content.info.size ?: 0L,
            caption = content.body,
            formattedCaption = null,
            isEdited = false,
            mediaSource = content.source,
            thumbnailSource = content.info.thumbnailSource,
            mimeType = content.info.mimetype ?: MimeTypes.Images,
            blurhash = content.info.blurhash,
            width = content.info.width?.toInt(),
            height = content.info.height?.toInt(),
            aspectRatio = aspectRatio,
            formattedFileSize = fileSizeFormatter.format(content.info.size ?: 0),
            fileExtension = fileExtensionExtractor.extractFromName(content.filename)
        )
    }
}
