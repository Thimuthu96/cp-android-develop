/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.ui.media

import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaSource

internal fun AvatarData.toMediaRequestData(): MediaRequestData {
    return MediaRequestData(
        source = url?.let { MediaSource(it) },
        kind = MediaRequestData.Kind.Thumbnail(AVATAR_THUMBNAIL_SIZE_IN_PIXEL)
    )
}
