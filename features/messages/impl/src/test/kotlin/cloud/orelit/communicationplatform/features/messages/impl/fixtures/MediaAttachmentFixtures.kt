/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.fixtures

import cloud.orelit.communicationplatform.features.messages.impl.attachments.Attachment
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.local.LocalMedia

fun aMediaAttachment(localMedia: LocalMedia, sendAsFile: Boolean = false) = Attachment.Media(
    localMedia = localMedia,
    sendAsFile = sendAsFile,
)
