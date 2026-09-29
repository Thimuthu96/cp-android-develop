/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaSource
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.AvatarInfo
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.MediaViewerEntryPoint
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.viewer.MediaViewerNode

@ContributesBinding(AppScope::class)
class DefaultMediaViewerEntryPoint : MediaViewerEntryPoint {
    override fun createParamsForAvatar(filename: String, avatarUrl: String): MediaViewerEntryPoint.Params {
        return MediaViewerEntryPoint.Params.Avatar(
            avatarInfo = AvatarInfo(
                filename = filename,
            ),
            mediaSource = MediaSource(url = avatarUrl),
            thumbnailSource = null,
            // Maybe add avatar blurhash if available?
            blurHash = null,
        )
    }

    override fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: MediaViewerEntryPoint.Params,
        callback: MediaViewerEntryPoint.Callback,
    ): Node {
        return parentNode.createNode<MediaViewerNode>(
            buildContext = buildContext,
            plugins = listOf(params, callback),
        )
    }
}
