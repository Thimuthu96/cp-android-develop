/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.local.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.utils.CommonDrawables
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.local.LocalMedia
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.local.LocalMediaViewState
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.local.rememberLocalMediaViewState
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState

@Composable
fun MediaImageView(
    localMediaViewState: LocalMediaViewState,
    localMedia: LocalMedia?,
    forPreview: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        Image(
            painter = painterResource(id = CommonDrawables.sample_background),
            modifier = modifier,
            contentDescription = null,
        )
    } else {
        val zoomableImageState = rememberZoomableImageState(localMediaViewState.zoomableState)
        localMediaViewState.isReady = zoomableImageState.isImageDisplayed
        val imageDescription = if (forPreview) {
            stringResource(CommonStrings.a11y_photo_preview)
        } else {
            stringResource(CommonStrings.common_image)
        }
        ZoomableAsyncImage(
            modifier = modifier,
            state = zoomableImageState,
            model = localMedia?.uri,
            contentDescription = imageDescription,
            contentScale = ContentScale.Fit,
            onClick = { onClick() }
        )
    }
}

@PreviewsDayNight
@Composable
internal fun MediaImageViewPreview() = ElementPreview {
    MediaImageView(
        modifier = Modifier.fillMaxSize(),
        localMediaViewState = rememberLocalMediaViewState(),
        localMedia = null,
        forPreview = false,
        onClick = {},
    )
}
