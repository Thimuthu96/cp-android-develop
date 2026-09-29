/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.gallery.di.LocalMediaItemPresenterFactories
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.gallery.di.MediaItemPresenterFactories
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.model.MediaItem

@ContributesNode(RoomScope::class)
@AssistedInject
class MediaGalleryNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: MediaGalleryPresenter.Factory,
    private val mediaItemPresenterFactories: MediaItemPresenterFactories,
) : Node(buildContext, plugins = plugins),
    MediaGalleryNavigator {
    private val presenter = presenterFactory.create(
        navigator = this,
    )

    interface Callback : Plugin {
        fun onBackClick()
        fun showItem(item: MediaItem.Event)
        fun viewInTimeline(eventId: EventId)
        fun forward(eventId: EventId)
    }

    private val callback: Callback = callback()

    override fun onViewInTimelineClick(eventId: EventId) {
        callback.viewInTimeline(eventId)
    }

    override fun onForwardClick(eventId: EventId) {
        callback.forward(eventId)
    }

    @Composable
    override fun View(modifier: Modifier) {
        CompositionLocalProvider(
            LocalMediaItemPresenterFactories provides mediaItemPresenterFactories,
        ) {
            val state = presenter.present()
            MediaGalleryView(
                state = state,
                onBackClick = callback::onBackClick,
                onItemClick = callback::showItem,
                modifier = modifier,
            )
        }
    }
}
