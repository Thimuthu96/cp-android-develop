/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.PinUnpinAction
import cloud.orelit.communicationplatform.appconfig.MessageComposerConfig
import cloud.orelit.communicationplatform.features.location.api.live.ActiveLiveLocationShareManager
import cloud.orelit.communicationplatform.features.location.api.live.isCurrentlySharing
import cloud.orelit.communicationplatform.features.messages.api.timeline.HtmlConverterProvider
import cloud.orelit.communicationplatform.features.messages.impl.MessagesState.Threads
import cloud.orelit.communicationplatform.features.messages.impl.actionlist.ActionListState
import cloud.orelit.communicationplatform.features.messages.impl.actionlist.model.TimelineItemAction
import cloud.orelit.communicationplatform.features.messages.impl.crypto.identity.IdentityChangeState
import cloud.orelit.communicationplatform.features.messages.impl.link.LinkState
import cloud.orelit.communicationplatform.features.messages.impl.messagecomposer.MessageComposerEvent
import cloud.orelit.communicationplatform.features.messages.impl.messagecomposer.MessageComposerState
import cloud.orelit.communicationplatform.features.messages.impl.pinned.banner.PinnedMessagesBannerState
import cloud.orelit.communicationplatform.features.messages.impl.timeline.MarkAsFullyRead
import cloud.orelit.communicationplatform.features.messages.impl.timeline.TimelineController
import cloud.orelit.communicationplatform.features.messages.impl.timeline.TimelineEvent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.TimelineState
import cloud.orelit.communicationplatform.features.messages.impl.timeline.components.customreaction.CustomReactionState
import cloud.orelit.communicationplatform.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryState
import cloud.orelit.communicationplatform.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetState
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.TimelineItem
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.TimelineItemThreadInfo
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemPollContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemStateContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.captionOrNull
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.htmlCaptionOrNull
import cloud.orelit.communicationplatform.features.messages.impl.timeline.protection.TimelineProtectionState
import cloud.orelit.communicationplatform.features.messages.impl.voicemessages.composer.DefaultVoiceMessageComposerPresenter
import cloud.orelit.communicationplatform.features.roomcall.api.RoomCallState
import cloud.orelit.communicationplatform.features.roommembermoderation.api.RoomMemberModerationEvent
import cloud.orelit.communicationplatform.features.roommembermoderation.api.RoomMemberModerationState
import cloud.orelit.communicationplatform.libraries.androidutils.clipboard.ClipboardHelper
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.core.coroutine.CoroutineDispatchers
import cloud.orelit.communicationplatform.libraries.core.extensions.flatMap
import cloud.orelit.communicationplatform.libraries.core.extensions.runCatchingExceptions
import cloud.orelit.communicationplatform.libraries.core.meta.BuildMeta
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarMessage
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import cloud.orelit.communicationplatform.libraries.di.annotations.SessionCoroutineScope
import cloud.orelit.communicationplatform.libraries.emoji.api.recentemojis.AddRecentEmoji
import cloud.orelit.communicationplatform.libraries.featureflag.api.FeatureFlagService
import cloud.orelit.communicationplatform.libraries.featureflag.api.FeatureFlags
import cloud.orelit.communicationplatform.libraries.matrix.api.core.toThreadId
import cloud.orelit.communicationplatform.libraries.matrix.api.encryption.EncryptionService
import cloud.orelit.communicationplatform.libraries.matrix.api.encryption.identity.IdentityState
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkParser
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomInfo
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMembersState
import cloud.orelit.communicationplatform.libraries.matrix.api.room.history.RoomHistoryVisibility
import cloud.orelit.communicationplatform.libraries.matrix.api.room.powerlevels.permissionsAsState
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import cloud.orelit.communicationplatform.libraries.matrix.ui.messages.reply.map
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.dmUserStatus
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getAvatarData
import cloud.orelit.communicationplatform.libraries.matrix.ui.room.getDirectRoomMember
import cloud.orelit.communicationplatform.libraries.textcomposer.model.MessageComposerMode
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

@AssistedInject
class MessagesPresenter(
    @Assisted private val navigator: MessagesNavigator,
    private val room: JoinedRoom,
    @Assisted private val composerPresenter: Presenter<MessageComposerState>,
    voiceMessageComposerPresenterFactory: DefaultVoiceMessageComposerPresenter.Factory,
    @Assisted private val timelinePresenter: Presenter<TimelineState>,
    private val timelineProtectionPresenter: Presenter<TimelineProtectionState>,
    private val identityChangeStatePresenter: Presenter<IdentityChangeState>,
    private val linkPresenter: Presenter<LinkState>,
    @Assisted private val actionListPresenter: Presenter<ActionListState>,
    private val customReactionPresenter: Presenter<CustomReactionState>,
    private val reactionSummaryPresenter: Presenter<ReactionSummaryState>,
    private val readReceiptBottomSheetPresenter: Presenter<ReadReceiptBottomSheetState>,
    private val pinnedMessagesBannerPresenter: Presenter<PinnedMessagesBannerState>,
    private val roomCallStatePresenter: Presenter<RoomCallState>,
    private val roomMemberModerationPresenter: Presenter<RoomMemberModerationState>,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val dispatchers: CoroutineDispatchers,
    private val clipboardHelper: ClipboardHelper,
    private val htmlConverterProvider: HtmlConverterProvider,
    private val buildMeta: BuildMeta,
    @Assisted private val timelineController: TimelineController,
    private val permalinkParser: PermalinkParser,
    private val analyticsService: AnalyticsService,
    private val encryptionService: EncryptionService,
    private val featureFlagService: FeatureFlagService,
    private val addRecentEmoji: AddRecentEmoji,
    private val markAsFullyRead: MarkAsFullyRead,
    private val liveLocationShareManager: ActiveLiveLocationShareManager,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) : Presenter<MessagesState> {
    @AssistedFactory
    interface Factory {
        fun create(
            navigator: MessagesNavigator,
            composerPresenter: Presenter<MessageComposerState>,
            timelinePresenter: Presenter<TimelineState>,
            actionListPresenter: Presenter<ActionListState>,
            timelineController: TimelineController,
        ): MessagesPresenter
    }

    private val voiceMessageComposerPresenter = voiceMessageComposerPresenterFactory.create(
        timelineMode = timelineController.mainTimelineMode()
    )

    private val markingAsReadAndExiting = AtomicBoolean(false)

    @Composable
    override fun present(): MessagesState {
        htmlConverterProvider.Update()

        val coroutineScope = rememberCoroutineScope()
        val roomInfo by room.roomInfoFlow.collectAsState()
        val localCoroutineScope = rememberCoroutineScope()
        val composerState = composerPresenter.present()
        val voiceMessageComposerState = voiceMessageComposerPresenter.present()
        val timelineState = timelinePresenter.present()
        val timelineProtectionState = timelineProtectionPresenter.present()
        val identityChangeState = identityChangeStatePresenter.present()
        val actionListState = actionListPresenter.present()
        val linkState = linkPresenter.present()
        val customReactionState = customReactionPresenter.present()
        val reactionSummaryState = reactionSummaryPresenter.present()
        val readReceiptBottomSheetState = readReceiptBottomSheetPresenter.present()
        val pinnedMessagesBannerState = pinnedMessagesBannerPresenter.present()
        val roomCallState = roomCallStatePresenter.present()
        val roomMemberModerationState = roomMemberModerationPresenter.present()
        val threadsList by produceState(persistentListOf()) {
            room.threadsListService.subscribeToItemUpdates()
                .onStart { room.threadsListService.paginate() }
                .collectLatest { value = it.toImmutableList() }
        }

        val canOpenThreadList by featureFlagService.isFeatureEnabledFlow(FeatureFlags.RoomThreadList).collectAsState(initial = false)
        val isCurrentlySharingLiveLocationInRoom by remember { liveLocationShareManager.isCurrentlySharing(room.roomId) }.collectAsState()

        val userEventPermissions by room.permissionsAsState(UserEventPermissions.DEFAULT) { perms ->
            perms.userEventPermissions()
        }

        val roomAvatar by remember {
            derivedStateOf { roomInfo.avatarData() }
        }
        val heroes by remember {
            derivedStateOf { roomInfo.heroes().toImmutableList() }
        }

        var hasDismissedInviteDialog by rememberSaveable {
            mutableStateOf(false)
        }
        LaunchedEffect(Unit) {
            // Remove the unread flag on entering but don't send read receipts
            // as those will be handled by the timeline.
            withContext(dispatchers.io) {
                room.setUnreadFlag(isUnread = false)

                // If for some reason the encryption state is unknown, fetch it
                if (roomInfo.isEncrypted == null) {
                    room.getUpdatedIsEncrypted()
                }
            }
        }

        val inviteProgress = remember { mutableStateOf<AsyncData<Unit>>(AsyncData.Uninitialized) }
        var showReinvitePrompt by remember { mutableStateOf(false) }
        val composerHasFocus by remember { derivedStateOf { composerState.textEditorState.hasFocus() } }
        LaunchedEffect(hasDismissedInviteDialog, composerHasFocus, roomInfo) {
            withContext(dispatchers.io) {
                showReinvitePrompt = !hasDismissedInviteDialog && composerHasFocus && roomInfo.isDm && roomInfo.activeMembersCount == 1L
            }
        }

        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()

        var dmUserVerificationState by remember { mutableStateOf<IdentityState?>(null) }

        val membersState by room.membersStateFlow.collectAsState()
        val dmRoomMember by room.getDirectRoomMember(membersState)
        val roomMemberIdentityStateChanges = identityChangeState.roomMemberIdentityStateChanges

        // The top bar should show a "history" icon if:
        //   * The room is encrypted, and:
        //   * The room's history_visibility allows future users to see content.
        val topBarSharedHistoryIcon = roomInfo.sharedHistoryIcon()

        LifecycleResumeEffect(dmRoomMember, roomInfo.isEncrypted) {
            if (roomInfo.isEncrypted == true) {
                val dmRoomMemberId = dmRoomMember?.userId
                localCoroutineScope.launch {
                    dmRoomMemberId?.let { userId ->
                        dmUserVerificationState = roomMemberIdentityStateChanges.find { it.identityRoomMember.userId == userId }?.identityState
                            ?: encryptionService.getUserIdentity(userId).getOrNull()
                    }
                }
            }
            onPauseOrDispose {}
        }

        fun handleEvent(event: MessagesEvent) {
            when (event) {
                is MessagesEvent.HandleAction -> {
                    localCoroutineScope.handleTimelineAction(
                        action = event.action,
                        targetEvent = event.event,
                        composerState = composerState,
                        enableTextFormatting = composerState.showTextFormatting,
                        timelineState = timelineState,
                        timelineProtectionState = timelineProtectionState,
                    )
                }
                is MessagesEvent.ToggleReaction -> {
                    localCoroutineScope.toggleReaction(event.emoji, event.eventOrTransactionId)
                }
                is MessagesEvent.InviteDialogDismissed -> {
                    hasDismissedInviteDialog = true

                    if (event.action == InviteDialogAction.Invite) {
                        localCoroutineScope.reinviteOtherUser(inviteProgress)
                    }
                }
                is MessagesEvent.OnUserClicked -> {
                    roomMemberModerationState.eventSink(RoomMemberModerationEvent.ShowActionsForUser(event.user))
                }
                MessagesEvent.StopLiveLocationShare -> {
                    localCoroutineScope.launch {
                        liveLocationShareManager.stopShare(room.roomId)
                            .onFailure {
                                Timber.e(it, "Failed to stop live location share for roomId=${room.roomId}")
                                snackbarDispatcher.post(SnackbarMessage(CommonStrings.common_error))
                            }
                    }
                }
                MessagesEvent.ShowLiveLocationShare -> {
                    navigator.navigateToCurrentLiveLocation()
                }
                is MessagesEvent.MarkAsFullyReadAndExit -> if (!markingAsReadAndExiting.getAndSet(true)) {
                    coroutineScope.launch {
                        val latestEventId = room.liveTimeline.getLatestEventId().getOrElse {
                            Timber.w(it, "Failed to get latest event id to mark as fully read")
                            null
                        }
                        latestEventId?.let { eventId ->
                            sessionCoroutineScope.launch {
                                markAsFullyRead(room.roomId, eventId)
                            }
                        }
                        navigator.close()
                    }.invokeOnCompletion {
                        markingAsReadAndExiting.set(false)
                    }
                }
            }
        }

        return MessagesState(
            roomId = room.roomId,
            roomName = roomInfo.name,
            roomAvatar = roomAvatar,
            heroes = heroes,
            userEventPermissions = userEventPermissions,
            composerState = composerState,
            voiceMessageComposerState = voiceMessageComposerState,
            timelineState = timelineState,
            timelineProtectionState = timelineProtectionState,
            identityChangeState = identityChangeState,
            linkState = linkState,
            actionListState = actionListState,
            customReactionState = customReactionState,
            reactionSummaryState = reactionSummaryState,
            readReceiptBottomSheetState = readReceiptBottomSheetState,
            snackbarMessage = snackbarMessage,
            inviteProgress = inviteProgress.value,
            showReinvitePrompt = showReinvitePrompt,
            enableTextFormatting = MessageComposerConfig.ENABLE_RICH_TEXT_EDITING,
            roomCallState = roomCallState,
            appName = buildMeta.applicationName,
            pinnedMessagesBannerState = pinnedMessagesBannerState,
            dmUserVerificationState = dmUserVerificationState,
            dmUserStatus = roomInfo.dmUserStatus(),
            roomMemberModerationState = roomMemberModerationState,
            topBarSharedHistoryIcon = topBarSharedHistoryIcon,
            successorRoom = roomInfo.successorRoom,
            threads = Threads(
                hasThreads = canOpenThreadList && threadsList.isNotEmpty(),
                // TODO calculate this properly based on the thread list and the read state of each thread
                hasUnreadThreads = false,
            ),
            showLiveLocationShareBanner = isCurrentlySharingLiveLocationInRoom && timelineState.timelineMode !is Timeline.Mode.Thread,
            eventSink = ::handleEvent,
        )
    }

    private fun RoomInfo.sharedHistoryIcon(): SharedHistoryIcon {
        if (isEncrypted == true) {
            if (historyVisibility == RoomHistoryVisibility.Shared) {
                return SharedHistoryIcon.SHARED
            } else if (historyVisibility == RoomHistoryVisibility.WorldReadable) {
                return SharedHistoryIcon.WORLD_READABLE
            }
        }

        return SharedHistoryIcon.NONE
    }

    private fun RoomInfo.avatarData(): AvatarData {
        return AvatarData(
            id = id.value,
            name = name,
            url = avatarUrl,
            size = AvatarSize.TimelineRoom
        )
    }

    private fun RoomInfo.heroes(): List<AvatarData> {
        return heroes.map { user ->
            user.getAvatarData(size = AvatarSize.TimelineRoom)
        }
    }

    private fun CoroutineScope.handleTimelineAction(
        action: TimelineItemAction,
        targetEvent: TimelineItem.Event,
        composerState: MessageComposerState,
        timelineProtectionState: TimelineProtectionState,
        enableTextFormatting: Boolean,
        timelineState: TimelineState,
    ) = launch {
        when (action) {
            TimelineItemAction.CopyText -> handleCopyContents(targetEvent)
            TimelineItemAction.CopyCaption -> handleCopyCaption(targetEvent)
            TimelineItemAction.CopyLink -> handleCopyLink(targetEvent)
            TimelineItemAction.Redact -> handleActionRedact(targetEvent)
            TimelineItemAction.Edit,
            TimelineItemAction.EditPoll -> handleActionEdit(targetEvent, composerState, enableTextFormatting)
            TimelineItemAction.AddCaption -> handleActionAddCaption(targetEvent, composerState)
            TimelineItemAction.EditCaption -> handleActionEditCaption(targetEvent, composerState, enableTextFormatting)
            TimelineItemAction.RemoveCaption -> handleRemoveCaption(targetEvent)
            TimelineItemAction.Reply -> handleActionReply(targetEvent, composerState, timelineProtectionState)
            TimelineItemAction.ReplyInThread -> {
                val displayThreads = featureFlagService.isFeatureEnabled(FeatureFlags.Threads)
                if (displayThreads) {
                    // Get either the thread id this event is in, or the event id if it's not in a thread so we can start one
                    val threadId = when (targetEvent.threadInfo) {
                        is TimelineItemThreadInfo.ThreadResponse -> targetEvent.threadInfo.threadRootId
                        is TimelineItemThreadInfo.ThreadRoot, null -> targetEvent.eventId?.toThreadId()
                    } ?: return@launch
                    navigator.navigateToThread(threadId, null)
                } else {
                    handleActionReply(targetEvent, composerState, timelineProtectionState)
                }
            }
            TimelineItemAction.ViewSource -> handleShowDebugInfoAction(targetEvent)
            TimelineItemAction.Forward -> handleForwardAction(targetEvent)
            TimelineItemAction.ReportContent -> handleReportAction(targetEvent)
            TimelineItemAction.EndPoll -> handleEndPollAction(targetEvent, timelineState)
            TimelineItemAction.Pin -> handlePinAction(targetEvent)
            TimelineItemAction.Unpin -> handleUnpinAction(targetEvent)
            TimelineItemAction.ViewInTimeline -> Unit
            TimelineItemAction.RetrySending -> handleRetrySending(targetEvent)
        }
    }

    private suspend fun handleRetrySending(targetEvent: TimelineItem.Event) {
        val sendHandle = targetEvent.sendhandle ?: return Unit.also {
            Timber.w("No send handle for event ${targetEvent.eventOrTransactionId}")
        }
        sendHandle.retry()
            .onSuccess {
                Timber.d("Succeed to add the message back to the send queue")
            }
            .onFailure {
                Timber.e(it, "Failed to add the message back to the send queue")
            }
    }

    private suspend fun handleRemoveCaption(targetEvent: TimelineItem.Event) {
        timelineController.invokeOnCurrentTimeline {
            editCaption(
                eventOrTransactionId = targetEvent.eventOrTransactionId,
                caption = null,
                formattedCaption = null,
            )
        }
    }

    private suspend fun handlePinAction(targetEvent: TimelineItem.Event) {
        if (targetEvent.eventId == null) return
        analyticsService.capture(
            PinUnpinAction(
                from = PinUnpinAction.From.Timeline,
                kind = PinUnpinAction.Kind.Pin,
            )
        )
        timelineController.invokeOnCurrentTimeline {
            pinEvent(targetEvent.eventId)
                .onFailure {
                    Timber.e(it, "Failed to pin event ${targetEvent.eventId}")
                    snackbarDispatcher.post(SnackbarMessage(CommonStrings.common_error))
                }
        }
    }

    private suspend fun handleUnpinAction(targetEvent: TimelineItem.Event) {
        if (targetEvent.eventId == null) return
        analyticsService.capture(
            PinUnpinAction(
                from = PinUnpinAction.From.Timeline,
                kind = PinUnpinAction.Kind.Unpin,
            )
        )
        timelineController.invokeOnCurrentTimeline {
            unpinEvent(targetEvent.eventId)
                .onFailure {
                    Timber.e(it, "Failed to unpin event ${targetEvent.eventId}")
                    snackbarDispatcher.post(SnackbarMessage(CommonStrings.common_error))
                }
        }
    }

    private fun CoroutineScope.toggleReaction(
        emoji: String,
        eventOrTransactionId: EventOrTransactionId,
    ) = launch(dispatchers.io) {
        timelineController.invokeOnCurrentTimeline {
            toggleReaction(emoji, eventOrTransactionId)
                .flatMap { added -> if (added) addRecentEmoji(emoji) else Result.success(Unit) }
                .onFailure { Timber.e(it) }
        }
    }

    private fun CoroutineScope.reinviteOtherUser(inviteProgress: MutableState<AsyncData<Unit>>) = launch(dispatchers.io) {
        inviteProgress.value = AsyncData.Loading()
        runCatchingExceptions {
            val memberList = when (val memberState = room.membersStateFlow.value) {
                is RoomMembersState.Ready -> memberState.roomMembers
                is RoomMembersState.Error -> memberState.prevRoomMembers.orEmpty()
                else -> emptyList()
            }

            val member = memberList.first { it.userId != room.sessionId }
            room.inviteUserById(member.userId).onFailure { t ->
                Timber.e(t, "Failed to reinvite DM partner")
            }.getOrThrow()
        }.fold(
            onSuccess = {
                inviteProgress.value = AsyncData.Success(Unit)
            },
            onFailure = {
                inviteProgress.value = AsyncData.Failure(it)
            }
        )
    }

    private suspend fun handleActionRedact(event: TimelineItem.Event) {
        timelineController.invokeOnCurrentTimeline {
            redactEvent(eventOrTransactionId = event.eventOrTransactionId, reason = null)
                .onFailure { Timber.e(it) }
        }
    }

    private fun handleActionEdit(
        targetEvent: TimelineItem.Event,
        composerState: MessageComposerState,
        enableTextFormatting: Boolean,
    ) {
        when (targetEvent.content) {
            is TimelineItemPollContent -> {
                if (targetEvent.eventId == null) return
                navigator.navigateToEditPoll(targetEvent.eventId)
            }
            else -> {
                val composerMode = MessageComposerMode.Edit(
                    targetEvent.eventOrTransactionId,
                    (targetEvent.content as? TimelineItemTextBasedContent)?.let {
                        if (enableTextFormatting) {
                            it.htmlBody ?: it.body
                        } else {
                            it.body
                        }
                    }.orEmpty(),
                )
                composerState.eventSink(
                    MessageComposerEvent.SetMode(composerMode)
                )
            }
        }
    }

    private suspend fun handleActionAddCaption(
        targetEvent: TimelineItem.Event,
        composerState: MessageComposerState,
    ) {
        val composerMode = MessageComposerMode.EditCaption(
            eventOrTransactionId = targetEvent.eventOrTransactionId,
            content = "",
        )
        composerState.eventSink(
            MessageComposerEvent.SetMode(composerMode)
        )
    }

    private suspend fun handleActionEditCaption(
        targetEvent: TimelineItem.Event,
        composerState: MessageComposerState,
        enableTextFormatting: Boolean,
    ) {
        val composerMode = MessageComposerMode.EditCaption(
            eventOrTransactionId = targetEvent.eventOrTransactionId,
            content = if (enableTextFormatting) {
                targetEvent.content.htmlCaptionOrNull() ?: targetEvent.content.captionOrNull()
            } else {
                targetEvent.content.captionOrNull()
            }.orEmpty(),
        )
        composerState.eventSink(
            MessageComposerEvent.SetMode(composerMode)
        )
    }

    private suspend fun handleActionReply(
        targetEvent: TimelineItem.Event,
        composerState: MessageComposerState,
        timelineProtectionState: TimelineProtectionState,
    ) {
        if (targetEvent.eventId == null) return
        timelineController.invokeOnCurrentTimeline {
            val replyToDetails = loadReplyDetails(targetEvent.eventId).map(permalinkParser)
            val composerMode = MessageComposerMode.Reply(
                replyToDetails = replyToDetails,
                hideImage = timelineProtectionState.hideMediaContent(targetEvent.eventId, targetEvent.isMine),
            )
            composerState.eventSink(
                MessageComposerEvent.SetMode(composerMode)
            )
        }
    }

    private fun handleShowDebugInfoAction(event: TimelineItem.Event) {
        navigator.navigateToEventDebugInfo(event.eventId, event.debugInfo)
    }

    private fun handleForwardAction(event: TimelineItem.Event) {
        if (event.eventId == null) return
        navigator.forwardEvent(event.eventId)
    }

    private fun handleReportAction(event: TimelineItem.Event) {
        if (event.eventId == null) return
        navigator.navigateToReportMessage(event.eventId, event.senderId)
    }

    private fun handleEndPollAction(
        event: TimelineItem.Event,
        timelineState: TimelineState,
    ) {
        event.eventId?.let { timelineState.eventSink(TimelineEvent.EndPoll(it)) }
    }

    private suspend fun handleCopyLink(event: TimelineItem.Event) {
        event.eventId ?: return
        room.getPermalinkFor(event.eventId).fold(
            onSuccess = { permalink ->
                clipboardHelper.copyPlainText(permalink)
                snackbarDispatcher.post(SnackbarMessage(CommonStrings.common_link_copied_to_clipboard))
            },
            onFailure = {
                Timber.e(it, "Failed to get permalink for event ${event.eventId}")
                snackbarDispatcher.post(SnackbarMessage(CommonStrings.common_error))
            }
        )
    }

    private fun handleCopyContents(event: TimelineItem.Event) {
        val content = when (event.content) {
            is TimelineItemTextBasedContent -> event.content.body
            is TimelineItemStateContent -> event.content.body
            else -> return
        }
        clipboardHelper.copyPlainText(content)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            snackbarDispatcher.post(SnackbarMessage(R.string.screen_room_timeline_message_copied))
        }
    }

    private fun handleCopyCaption(event: TimelineItem.Event) {
        val content = event.content.captionOrNull() ?: return
        clipboardHelper.copyPlainText(content)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            snackbarDispatcher.post(SnackbarMessage(CommonStrings.common_copied_to_clipboard))
        }
    }
}
