/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.test.viewer

import android.net.Uri
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.MediaInfo
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.anImageMediaInfo
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.local.LocalMedia

fun aLocalMedia(
    uri: Uri,
    mediaInfo: MediaInfo = anImageMediaInfo(),
) = LocalMedia(
    uri = uri,
    info = mediaInfo
)
