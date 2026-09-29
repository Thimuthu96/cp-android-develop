/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.pinned.list

import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.features.messages.impl.actionlist.ActionListPresenter
import cloud.orelit.communicationplatform.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import cloud.orelit.communicationplatform.features.messages.impl.timeline.di.TimelineItemPresenterFactories
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.TimelineItem
import cloud.orelit.communicationplatform.libraries.androidutils.system.copyToClipboard
import cloud.orelit.communicationplatform.libraries.androidutils.system.openUrlInExternalApp
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.emoji.api.picker.EmojiPickerRenderer
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.ThreadId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkData
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkParser
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.libraries.ui.utils.a11y.hasExternalKeyboard
import cloud.orelit.communicationplatform.libraries.ui.utils.a11y.isTalkbackActive

@ContributesNode(RoomScope::class)
@AssistedInject
class PinnedMessagesListNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: PinnedMessagesListPresenter.Factory,
    actionListPresenterFactory: ActionListPresenter.Factory,
    private val timelineItemPresenterFactories: TimelineItemPresenterFactories,
    private val permalinkParser: PermalinkParser,
    private val emojiPickerRenderer: EmojiPickerRenderer,
) : Node(buildContext, plugins = plugins), PinnedMessagesListNavigator {
    interface Callback : Plugin {
        fun handleEventClick(event: TimelineItem.Event, canUseOverlay: Boolean)
        fun handleGalleryItemClick(event: TimelineItem.Event, galleryItemIndex: Int, canUseOverlay: Boolean)
        fun navigateToRoomMemberDetails(userId: UserId)
        fun viewInTimeline(eventId: EventId)
        fun handlePermalinkClick(data: PermalinkData.RoomLink)
        fun navigateToEventDebugInfo(eventId: EventId?, debugInfo: TimelineItemDebugInfo)
        fun handleForwardEventClick(eventId: EventId)
        fun navigateToThread(threadRootId: ThreadId)
    }

    private val callback: Callback = callback()
    private val presenter = presenterFactory.create(
        navigator = this,
        actionListPresenter = actionListPresenterFactory.create(
            postProcessor = PinnedMessagesListTimelineActionPostProcessor(),
            timelineMode = Timeline.Mode.PinnedEvents,
        )
    )

    private fun onLinkClick(context: Context, url: String) {
        when (val permalink = permalinkParser.parse(url)) {
            is PermalinkData.UserLink -> {
                // Open the room member profile, it will fallback to
                // the user profile if the user is not in the room
                callback.navigateToRoomMemberDetails(permalink.userId)
            }
            is PermalinkData.RoomLink -> {
                callback.handlePermalinkClick(permalink)
            }
            is PermalinkData.FallbackLink,
            is PermalinkData.RoomEmailInviteLink -> {
                context.openUrlInExternalApp(url)
            }
        }
    }

    override fun viewInTimeline(eventId: EventId) {
        callback.viewInTimeline(eventId)
    }

    override fun navigateToEventDebugInfo(eventId: EventId?, debugInfo: TimelineItemDebugInfo) {
        callback.navigateToEventDebugInfo(eventId, debugInfo)
    }

    override fun forwardEvent(eventId: EventId) {
        callback.handleForwardEventClick(eventId)
    }

    override fun navigateToThread(threadRootId: ThreadId) {
        callback.navigateToThread(threadRootId)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val canUseOverlay = !isTalkbackActive() && !hasExternalKeyboard()
        CompositionLocalProvider(
            LocalTimelineItemPresenterFactories provides timelineItemPresenterFactories,
        ) {
            val context = LocalContext.current
            val toastMessage = stringResource(CommonStrings.common_copied_to_clipboard)
            val view = LocalView.current
            val state = presenter.present()
            PinnedMessagesListView(
                state = state,
                onBackClick = ::navigateUp,
                onEventClick = {
                    callback.handleEventClick(it, canUseOverlay)
                },
                onGalleryItemClick = { event, index ->
                    callback.handleGalleryItemClick(event, index, canUseOverlay)
                },
                onUserDataClick = { callback.navigateToRoomMemberDetails(it.userId) },
                onLinkClick = { link -> onLinkClick(context, link.url) },
                onLinkLongClick = {
                    view.performHapticFeedback(
                        HapticFeedbackConstants.LONG_PRESS
                    )
                    context.copyToClipboard(
                        text = it.url,
                        toastMessage = toastMessage,
                    )
                },
                emojiPickerRenderer = emojiPickerRenderer,
                modifier = modifier
            )
        }
    }
}
