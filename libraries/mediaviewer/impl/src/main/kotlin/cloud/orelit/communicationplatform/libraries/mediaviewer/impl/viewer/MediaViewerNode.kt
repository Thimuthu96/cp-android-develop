/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.viewer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.compound.colors.SemanticColorsLightDark
import cloud.orelit.communicationplatform.compound.theme.ForcedDarkElementTheme
import cloud.orelit.communicationplatform.features.contentscanner.api.ContentScannerService
import cloud.orelit.communicationplatform.features.enterprise.api.EnterpriseService
import cloud.orelit.communicationplatform.features.viewfolder.api.TextFileViewer
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.architecture.inputs
import cloud.orelit.communicationplatform.libraries.audio.api.AudioFocus
import cloud.orelit.communicationplatform.libraries.core.coroutine.CoroutineDispatchers
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MatrixMediaLoader
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.EventContentValidationCache
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.NoopContentValidationState
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.MediaViewerEntryPoint
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.local.LocalMediaFactory
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.datasource.FocusedTimelineMediaGalleryDataSourceFactory
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.datasource.TimelineMediaGalleryDataSource
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.model.hasEvent
import cloud.orelit.communicationplatform.services.toolbox.api.systemclock.SystemClock

@ContributesNode(RoomScope::class)
@AssistedInject
class MediaViewerNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: MediaViewerPresenter.Factory,
    timelineMediaGalleryDataSource: TimelineMediaGalleryDataSource,
    focusedTimelineMediaGalleryDataSourceFactory: FocusedTimelineMediaGalleryDataSourceFactory,
    mediaLoader: MatrixMediaLoader,
    localMediaFactory: LocalMediaFactory,
    coroutineDispatchers: CoroutineDispatchers,
    systemClock: SystemClock,
    pagerKeysHandler: PagerKeysHandler,
    private val textFileViewer: TextFileViewer,
    private val audioFocus: AudioFocus,
    private val sessionId: SessionId,
    private val enterpriseService: EnterpriseService,
    private val contentScannerService: ContentScannerService,
    private val contentValidationCache: EventContentValidationCache,
) : Node(buildContext, plugins = plugins),
    MediaViewerNavigator {
    private val callback: MediaViewerEntryPoint.Callback = callback()
    private val inputs = inputs<MediaViewerEntryPoint.Params>()

    override fun onViewInTimelineClick(eventId: EventId) {
        callback.viewInTimeline(eventId)
    }

    override fun onForwardClick(eventId: EventId, fromPinnedEvents: Boolean) {
        callback.forwardEvent(eventId, fromPinnedEvents)
    }

    override fun onItemDeleted() {
        callback.onDone()
    }

    private val mediaGallerySource = when (inputs) {
        is MediaViewerEntryPoint.Params.Avatar ->
            SingleMediaGalleryDataSource.createFrom(inputs, contentValidationCache)
        is MediaViewerEntryPoint.Params.EventGallery ->
            GalleryMediaGalleryDataSource.createFrom(
                eventId = inputs.eventId,
                galleryItems = inputs.galleryItems,
                galleryInfo = inputs.galleryInfo,
                contentValidationState = inputs.eventId?.let { contentValidationCache[it] } ?: NoopContentValidationState(),
            )
        is MediaViewerEntryPoint.Params.RoomMedia -> {
            val eventId = inputs.eventId
            if (eventId == null) {
                // Local echo, the viewer will not be focused on the item
                timelineMediaGalleryDataSource
            } else {
                // Can we use a specific timeline?
                val timelineMode = inputs.mode.getTimelineMode()
                when (timelineMode) {
                    Timeline.Mode.Live,
                    is Timeline.Mode.FocusedOnEvent,
                    is Timeline.Mode.Thread -> {
                        // Does timelineMediaGalleryDataSource knows the eventId?
                        val lastData = timelineMediaGalleryDataSource.getLastData().dataOrNull()
                        val isEventKnown = lastData?.hasEvent(eventId) == true
                        if (isEventKnown) {
                            timelineMediaGalleryDataSource
                        } else {
                            focusedTimelineMediaGalleryDataSourceFactory.createFor(
                                eventId = eventId,
                                mediaItem = inputs.toMediaItem(contentValidationCache[eventId]),
                                onlyPinnedEvents = false,
                            )
                        }
                    }
                    Timeline.Mode.PinnedEvents -> {
                        focusedTimelineMediaGalleryDataSourceFactory.createFor(
                            eventId = eventId,
                            mediaItem = inputs.toMediaItem(contentValidationCache[eventId]),
                            onlyPinnedEvents = true,
                        )
                    }
                    Timeline.Mode.Media -> timelineMediaGalleryDataSource
                    // null should not happen, input should be MediaViewerEntryPoint.Params.EventGallery in this case
                    null -> timelineMediaGalleryDataSource
                }
            }
        }
    }

    private val presenter = presenterFactory.create(
        inputs = inputs,
        navigator = this,
        dataSource = MediaViewerDataSource(
            mode = when (inputs) {
                is MediaViewerEntryPoint.Params.Avatar ->
                    MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos(Timeline.Mode.Media)
                is MediaViewerEntryPoint.Params.EventGallery ->
                    MediaViewerEntryPoint.MediaViewerMode.EventGallery(fromPinnedMessages = inputs.fromPinnedMessages)
                is MediaViewerEntryPoint.Params.RoomMedia ->
                    inputs.mode
            },
            coroutineScope = lifecycleScope,
            dispatcher = coroutineDispatchers.computation,
            galleryDataSource = mediaGallerySource,
            mediaLoader = mediaLoader,
            localMediaFactory = localMediaFactory,
            systemClock = systemClock,
            pagerKeysHandler = pagerKeysHandler,
            contentScannerService = contentScannerService,
            contentValidationCache = contentValidationCache,
        )
    )

    @Composable
    override fun View(modifier: Modifier) {
        val colors by remember {
            enterpriseService.semanticColorsFlow(sessionId = sessionId)
        }.collectAsState(SemanticColorsLightDark.default)
        ForcedDarkElementTheme(
            colors = colors,
        ) {
            val state = presenter.present()
            MediaViewerView(
                state = state,
                textFileViewer = textFileViewer,
                modifier = modifier,
                audioFocus = audioFocus,
                onBackClick = callback::onDone,
            )
        }
    }
}

internal fun MediaViewerEntryPoint.MediaViewerMode.getTimelineMode(): Timeline.Mode? {
    return when (this) {
        is MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos -> timelineMode
        is MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios -> timelineMode
        is MediaViewerEntryPoint.MediaViewerMode.EventGallery -> null
    }
}
