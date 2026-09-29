/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.model

import cloud.orelit.communicationplatform.libraries.designsystem.components.media.WaveFormSamples
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UniqueId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaSource
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.ContentValidationState
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.NoopContentValidationState
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.aPdfMediaInfo
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.aVideoMediaInfo
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.aVoiceMediaInfo
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.anAudioMediaInfo
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.anImageMediaInfo

fun aMediaItemImage(
    id: UniqueId = UniqueId("imageId"),
    eventId: EventId? = null,
    senderId: UserId? = null,
    mediaSourceUrl: String = "",
    validationState: ContentValidationState = NoopContentValidationState(),
    blurHash: String? = null,
): MediaItem.Image {
    return MediaItem.Image(
        id = id,
        eventId = eventId,
        mediaInfo = anImageMediaInfo(
            senderId = senderId,
        ),
        mediaSource = MediaSource(mediaSourceUrl),
        thumbnailSource = null,
        blurHash = blurHash,
        validationState = validationState,
    )
}

fun aMediaItemVideo(
    id: UniqueId = UniqueId("videoId"),
    mediaSource: MediaSource = MediaSource(""),
    duration: String? = "1:23",
    validationState: ContentValidationState = NoopContentValidationState(),
): MediaItem.Video {
    return MediaItem.Video(
        id = id,
        eventId = null,
        mediaInfo = aVideoMediaInfo(
            duration = duration
        ),
        mediaSource = mediaSource,
        thumbnailSource = null,
        blurHash = null,
        validationState = validationState,
    )
}

fun aMediaItemFile(
    id: UniqueId = UniqueId("fileId"),
    eventId: EventId? = null,
    filename: String = "filename",
    caption: String? = null,
    validationState: ContentValidationState = NoopContentValidationState(),
): MediaItem.File {
    return MediaItem.File(
        id = id,
        eventId = eventId,
        mediaInfo = aPdfMediaInfo(
            filename = filename,
            caption = caption,
        ),
        mediaSource = MediaSource(""),
        validationState = validationState,
    )
}

fun aMediaItemAudio(
    id: UniqueId = UniqueId("fileId"),
    eventId: EventId? = null,
    filename: String = "filename",
    caption: String? = null,
    validationState: ContentValidationState = NoopContentValidationState(),
): MediaItem.Audio {
    return MediaItem.Audio(
        id = id,
        eventId = eventId,
        mediaInfo = anAudioMediaInfo(
            filename = filename,
            caption = caption,
        ),
        mediaSource = MediaSource(""),
        validationState = validationState,
    )
}

fun aMediaItemVoice(
    id: UniqueId = UniqueId("fileId"),
    filename: String = "filename.ogg",
    caption: String? = null,
    duration: String? = "1:23",
    waveform: List<Float> = WaveFormSamples.realisticWaveForm,
    validationState: ContentValidationState = NoopContentValidationState(),
): MediaItem.Voice {
    return MediaItem.Voice(
        id = id,
        eventId = null,
        mediaInfo = aVoiceMediaInfo(
            filename = filename,
            caption = caption,
            duration = duration,
            waveForm = waveform,
        ),
        mediaSource = MediaSource(""),
        validationState = validationState,
    )
}

fun aMediaItemDateSeparator(
    id: UniqueId = UniqueId("dateId"),
    formattedDate: String = "October 2024",
): MediaItem.DateSeparator {
    return MediaItem.DateSeparator(
        id = id,
        formattedDate = formattedDate,
    )
}

fun aMediaItemLoadingIndicator(
    id: UniqueId = UniqueId("loadingId"),
    direction: Timeline.PaginationDirection = Timeline.PaginationDirection.BACKWARDS,
): MediaItem.LoadingIndicator {
    return MediaItem.LoadingIndicator(
        id = id,
        direction = direction,
        timestamp = 123,
    )
}
