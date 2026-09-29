/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.features.knockrequests.api.banner.KnockRequestsBannerRenderer
import cloud.orelit.communicationplatform.features.messages.impl.actionlist.ActionListPresenter
import cloud.orelit.communicationplatform.features.messages.impl.actionlist.model.TimelineItemActionPostProcessor
import cloud.orelit.communicationplatform.features.messages.impl.attachments.Attachment
import cloud.orelit.communicationplatform.features.messages.impl.messagecomposer.MessageComposerEvent
import cloud.orelit.communicationplatform.features.messages.impl.messagecomposer.MessageComposerPresenter
import cloud.orelit.communicationplatform.features.messages.impl.timeline.TimelineController
import cloud.orelit.communicationplatform.features.messages.impl.timeline.TimelineEvent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.TimelinePresenter
import cloud.orelit.communicationplatform.features.messages.impl.timeline.components.customreaction.CustomReactionBottomSheet
import cloud.orelit.communicationplatform.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import cloud.orelit.communicationplatform.features.messages.impl.timeline.di.TimelineItemPresenterFactories
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.TimelineItem
import cloud.orelit.communicationplatform.features.roommembermoderation.api.ModerationAction
import cloud.orelit.communicationplatform.features.roommembermoderation.api.RoomMemberModerationEvent
import cloud.orelit.communicationplatform.features.roommembermoderation.api.RoomMemberModerationRenderer
import cloud.orelit.communicationplatform.libraries.androidutils.browser.openUrlInChromeCustomTab
import cloud.orelit.communicationplatform.libraries.androidutils.system.openUrlInExternalApp
import cloud.orelit.communicationplatform.libraries.androidutils.system.toast
import cloud.orelit.communicationplatform.libraries.architecture.NodeInputs
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.architecture.inputs
import cloud.orelit.communicationplatform.libraries.designsystem.utils.OnLifecycleEvent
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.di.annotations.ApplicationContext
import cloud.orelit.communicationplatform.libraries.di.annotations.SessionCoroutineScope
import cloud.orelit.communicationplatform.libraries.emoji.api.picker.EmojiPickerRenderer
import cloud.orelit.communicationplatform.libraries.matrix.api.analytics.toAnalyticsViewRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.ThreadId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.toRoomIdOrAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkData
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkParser
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.room.alias.matches
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.EventContentValidationCache
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.LocalEventContentValidationState
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getBestName
import cloud.orelit.communicationplatform.libraries.mediaplayer.api.MediaPlayer
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.libraries.ui.utils.a11y.hasExternalKeyboard
import cloud.orelit.communicationplatform.libraries.ui.utils.a11y.isTalkbackActive
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsLongRunningTransaction.LoadMessagesUi
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService
import cloud.orelit.communicationplatform.services.analytics.api.finishLongRunningTransaction
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ContributesNode(RoomScope::class)
@AssistedInject
class MessagesNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    @ApplicationContext private val context: Context,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
    private val room: JoinedRoom,
    private val analyticsService: AnalyticsService,
    messageComposerPresenterFactory: MessageComposerPresenter.Factory,
    timelinePresenterFactory: TimelinePresenter.Factory,
    presenterFactory: MessagesPresenter.Factory,
    actionListPresenterFactory: ActionListPresenter.Factory,
    private val timelineItemPresenterFactories: TimelineItemPresenterFactories,
    private val mediaPlayer: MediaPlayer,
    private val permalinkParser: PermalinkParser,
    private val knockRequestsBannerRenderer: KnockRequestsBannerRenderer,
    private val roomMemberModerationRenderer: RoomMemberModerationRenderer,
    private val eventContentValidationCache: EventContentValidationCache,
    private val emojiPickerRenderer: EmojiPickerRenderer,
) : Node(buildContext, plugins = plugins), MessagesNavigator {
    data class Inputs(
        val focusedEventId: EventId?,
    ) : NodeInputs

    private val inputs = inputs<Inputs>()
    private val callback: Callback = callback()

    private val timelineController = TimelineController(room, room.liveTimeline)
    private val presenter = presenterFactory.create(
        navigator = this,
        composerPresenter = messageComposerPresenterFactory.create(timelineController, this, threadRoot = null),
        timelinePresenter = timelinePresenterFactory.create(timelineController = timelineController, this),
        actionListPresenter = actionListPresenterFactory.create(
            postProcessor = TimelineItemActionPostProcessor.Default,
            timelineMode = timelineController.mainTimelineMode(),
        ),
        timelineController = timelineController,
    )

    interface Callback : Plugin {
        fun handleEventClick(timelineMode: Timeline.Mode, event: TimelineItem.Event, canUseOverlay: Boolean): Boolean
        fun handleGalleryItemClick(timelineMode: Timeline.Mode, event: TimelineItem.Event, galleryItemIndex: Int, canUseOverlay: Boolean): Boolean
        fun navigateToPreviewAttachments(attachments: ImmutableList<Attachment>, inReplyToEventId: EventId?)
        fun navigateToRoomMemberDetails(userId: UserId)
        fun handlePermalinkClick(data: PermalinkData)
        fun navigateToEventDebugInfo(eventId: EventId?, debugInfo: TimelineItemDebugInfo)
        fun forwardEvent(eventId: EventId)
        fun navigateToReportMessage(eventId: EventId, senderId: UserId)
        fun navigateToSendLocation()
        fun navigateToCreatePoll()
        fun navigateToEditPoll(eventId: EventId)
        fun navigateToCurrentLiveLocation()
        fun navigateToRoomCall(roomId: RoomId, isAudioCall: Boolean)
        fun navigateToThread(threadRootId: ThreadId, focusedEventId: EventId?)
        fun navigateToRoomDetails()
        fun navigateToPinnedMessagesList()
        fun navigateToKnockRequestsList()
        fun navigateToDeveloperSettings()

        fun navigateToThreadsList()

        fun navigateToAvatarPreview(username: String, avatarUrl: String)
    }

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onCreate = {
                sessionCoroutineScope.launch { analyticsService.capture(room.toAnalyticsViewRoom()) }
            },
            onResume = {
                analyticsService.finishLongRunningTransaction(LoadMessagesUi)
            },
            onDestroy = {
                mediaPlayer.close()
            }
        )
    }

    private fun onLinkClick(
        activity: Activity,
        darkTheme: Boolean,
        url: String,
        eventSink: (TimelineEvent) -> Unit,
        customTab: Boolean
    ) {
        when (val permalink = permalinkParser.parse(url)) {
            is PermalinkData.UserLink -> {
                // Open the room member profile, it will fallback to
                // the user profile if the user is not in the room
                callback.navigateToRoomMemberDetails(permalink.userId)
            }
            is PermalinkData.RoomLink -> {
                handleRoomLinkClick(permalink, eventSink)
            }
            is PermalinkData.FallbackLink -> {
                if (customTab) {
                    activity.openUrlInChromeCustomTab(null, darkTheme, url)
                } else {
                    activity.openUrlInExternalApp(url)
                }
            }
            is PermalinkData.RoomEmailInviteLink -> {
                activity.openUrlInChromeCustomTab(null, darkTheme, url)
            }
        }
    }

    private fun handleRoomLinkClick(
        roomLink: PermalinkData.RoomLink,
        eventSink: (TimelineEvent) -> Unit,
    ) {
        if (room.matches(roomLink.roomIdOrAlias)) {
            val eventId = roomLink.eventId
            if (eventId != null) {
                eventSink(TimelineEvent.FocusOnEvent(eventId))
            } else {
                // Click on the same room, ignore
                displaySameRoomToast()
            }
        } else {
            callback.handlePermalinkClick(roomLink)
        }
    }

    override fun navigateToEventDebugInfo(eventId: EventId?, debugInfo: TimelineItemDebugInfo) {
        callback.navigateToEventDebugInfo(eventId, debugInfo)
    }

    override fun forwardEvent(eventId: EventId) {
        callback.forwardEvent(eventId)
    }

    override fun navigateToReportMessage(eventId: EventId, senderId: UserId) {
        callback.navigateToReportMessage(eventId, senderId)
    }

    override fun navigateToEditPoll(eventId: EventId) {
        callback.navigateToEditPoll(eventId)
    }

    override fun navigateToPreviewAttachments(attachments: ImmutableList<Attachment>, inReplyToEventId: EventId?) {
        callback.navigateToPreviewAttachments(attachments, inReplyToEventId)
    }

    override fun navigateToRoom(roomId: RoomId, eventId: EventId?, serverNames: List<String>) {
        if (roomId == room.roomId) {
            displaySameRoomToast()
        } else {
            val permalinkData = PermalinkData.RoomLink(roomId.toRoomIdOrAlias(), eventId, viaParameters = serverNames.toImmutableList())
            callback.handlePermalinkClick(permalinkData)
        }
    }

    override fun navigateToMember(userId: UserId) {
        callback.navigateToRoomMemberDetails(userId)
    }

    override fun navigateToThread(threadRootId: ThreadId, focusedEventId: EventId?) {
        callback.navigateToThread(threadRootId, focusedEventId)
    }

    override fun navigateToDeveloperSettings() {
        callback.navigateToDeveloperSettings()
    }

    override fun navigateToCurrentLiveLocation() {
        callback.navigateToCurrentLiveLocation()
    }

    private fun displaySameRoomToast() {
        context.toast(CommonStrings.screen_room_permalink_same_room_android)
    }

    override fun close() = navigateUp()

    @Composable
    override fun View(modifier: Modifier) {
        val activity = requireNotNull(LocalActivity.current)
        val isDark = ElementTheme.isLightTheme.not()
        val canUseOverlay = !isTalkbackActive() && !hasExternalKeyboard()
        CompositionLocalProvider(
            LocalTimelineItemPresenterFactories provides timelineItemPresenterFactories,
            LocalEventContentValidationState provides eventContentValidationCache,
        ) {
            val state = presenter.present()

            BackHandler {
                state.eventSink(MessagesEvent.MarkAsFullyReadAndExit)
            }

            OnLifecycleEvent { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> state.composerState.eventSink(MessageComposerEvent.SaveDraft)
                    else -> Unit
                }
            }
            MessagesView(
                state = state,
                onBackClick = { state.eventSink(MessagesEvent.MarkAsFullyReadAndExit) },
                onRoomDetailsClick = callback::navigateToRoomDetails,
                onEventContentClick = { isLive, event ->
                    if (isLive) {
                        callback.handleEventClick(timelineController.mainTimelineMode(), event, canUseOverlay)
                    } else {
                        val detachedTimelineMode = timelineController.detachedTimelineMode()
                        if (detachedTimelineMode != null) {
                            callback.handleEventClick(detachedTimelineMode, event, canUseOverlay)
                        } else {
                            false
                        }
                    }
                },
                onGalleryEventItemClick = { isLive, event, index ->
                    if (isLive) {
                        callback.handleGalleryItemClick(timelineController.mainTimelineMode(), event, index, canUseOverlay)
                    } else {
                        val detachedTimelineMode = timelineController.detachedTimelineMode()
                        if (detachedTimelineMode != null) {
                            callback.handleGalleryItemClick(detachedTimelineMode, event, index, canUseOverlay)
                        } else {
                            false
                        }
                    }
                },
                onUserDataClick = callback::navigateToRoomMemberDetails,
                onLinkClick = { url, customTab ->
                    onLinkClick(
                        activity = activity,
                        darkTheme = isDark,
                        url = url,
                        eventSink = state.timelineState.eventSink,
                        customTab = customTab,
                    )
                },
                onSendLocationClick = callback::navigateToSendLocation,
                onCreatePollClick = callback::navigateToCreatePoll,
                onJoinCallClick = { isAudioCall ->
                    callback.navigateToRoomCall(room.roomId, isAudioCall)
                },
                onViewAllPinnedMessagesClick = callback::navigateToPinnedMessagesList,
                modifier = modifier,
                knockRequestsBannerView = {
                    knockRequestsBannerRenderer.View(
                        modifier = Modifier,
                        onViewRequestsClick = callback::navigateToKnockRequestsList,
                    )
                },
                customReactionBottomSheet = {
                    CustomReactionBottomSheet(
                        state = state.customReactionState,
                        onSelectEmoji = { uniqueId, emoji ->
                            state.eventSink(MessagesEvent.ToggleReaction(emoji.unicode, uniqueId))
                        },
                        emojiPickerRenderer = emojiPickerRenderer,
                    )
                },
                onThreadsListClick = callback::navigateToThreadsList,
            )
            roomMemberModerationRenderer.Render(
                state = state.roomMemberModerationState,
                onSelectAction = { action, target ->
                    when (action) {
                        is ModerationAction.DisplayProfile -> callback.navigateToRoomMemberDetails(target.userId)
                        else -> state.roomMemberModerationState.eventSink(RoomMemberModerationEvent.ProcessAction(action, target))
                    }
                },
                onAvatarClick = { user ->
                    user.avatarUrl?.let { url ->
                        callback.navigateToAvatarPreview(user.getBestName(), url)
                    }
                },
                modifier = Modifier,
            )

            var focusedEventId by rememberSaveable {
                mutableStateOf(inputs.focusedEventId)
            }
            LaunchedEffect(focusedEventId) {
                if (focusedEventId != null) {
                    state.timelineState.eventSink(TimelineEvent.FocusOnEvent(focusedEventId!!))
                    focusedEventId = null
                }
            }
        }
    }
}
