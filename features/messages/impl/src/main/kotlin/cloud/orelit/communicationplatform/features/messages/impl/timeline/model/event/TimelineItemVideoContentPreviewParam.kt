/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.libraries.core.mimetype.MimeTypes
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaSource
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.A_BLUR_HASH
import kotlin.time.Duration.Companion.milliseconds

open class TimelineItemVideoContentPreviewParam : PreviewParameterProvider<TimelineItemVideoContent> {
    override val values: Sequence<TimelineItemVideoContent>
        get() = sequenceOf(
            aTimelineItemVideoContent(),
            aTimelineItemVideoContent(aspectRatio = 1.0f),
            aTimelineItemVideoContent(aspectRatio = 1.5f),
            aTimelineItemVideoContent(thumbnailMediaSource = MediaSource("https://example.com/thumbnail.jpg")),
            aTimelineItemVideoContent(blurhash = null),
        )
}

fun aTimelineItemVideoContent(
    aspectRatio: Float = 0.5f,
    blurhash: String? = A_BLUR_HASH,
    caption: String? = null,
    mediaSource: MediaSource = MediaSource(""),
    thumbnailMediaSource: MediaSource? = null,
) = TimelineItemVideoContent(
    filename = "Video.mp4",
    fileSize = 14 * 1024 * 1024L,
    caption = caption,
    formattedCaption = null,
    isEdited = false,
    thumbnailSource = thumbnailMediaSource,
    blurHash = blurhash,
    aspectRatio = aspectRatio,
    duration = 100.milliseconds,
    mediaSource = mediaSource,
    width = 150,
    height = 300,
    thumbnailWidth = 150,
    thumbnailHeight = 300,
    mimeType = MimeTypes.Mp4,
    formattedFileSize = "14MB",
    fileExtension = "mp4"
)
