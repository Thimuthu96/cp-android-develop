/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event

import cloud.orelit.communicationplatform.libraries.core.mimetype.MimeTypes.isMimeTypeAnimatedImage
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaSource
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.MAX_THUMBNAIL_HEIGHT
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.MAX_THUMBNAIL_WIDTH
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.MediaRequestData

data class TimelineItemImageContent(
    override val filename: String,
    override val fileSize: Long?,
    override val caption: String?,
    override val formattedCaption: CharSequence?,
    override val htmlCaption: String? = null,
    override val isEdited: Boolean,
    override val mediaSource: MediaSource,
    val thumbnailSource: MediaSource?,
    override val formattedFileSize: String,
    override val fileExtension: String,
    override val mimeType: String,
    val blurhash: String?,
    val width: Int?,
    val height: Int?,
    val thumbnailWidth: Int?,
    val thumbnailHeight: Int?,
    val aspectRatio: Float?
) : TimelineItemEventContentWithAttachment {
    override val type: String = "TimelineItemImageContent"

    val showCaption = caption != null

    val thumbnailMediaRequestData: MediaRequestData by lazy {
        if (mimeType.isMimeTypeAnimatedImage()) {
            MediaRequestData(
                source = mediaSource,
                kind = MediaRequestData.Kind.File(
                    fileName = filename,
                    mimeType = mimeType
                )
            )
        } else {
            MediaRequestData(
                source = thumbnailSource ?: mediaSource,
                kind = MediaRequestData.Kind.Thumbnail(
                    width = thumbnailWidth?.toLong() ?: MAX_THUMBNAIL_WIDTH,
                    height = thumbnailHeight?.toLong() ?: MAX_THUMBNAIL_HEIGHT
                ),
            )
        }
    }
}
