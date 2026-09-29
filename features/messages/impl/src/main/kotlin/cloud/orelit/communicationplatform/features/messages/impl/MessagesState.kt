/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl

import cloud.orelit.communicationplatform.features.messages.api.timeline.voicemessages.composer.VoiceMessageComposerState
import cloud.orelit.communicationplatform.features.messages.impl.actionlist.ActionListState
import cloud.orelit.communicationplatform.features.messages.impl.crypto.identity.IdentityChangeState
import cloud.orelit.communicationplatform.features.messages.impl.link.LinkState
import cloud.orelit.communicationplatform.features.messages.impl.messagecomposer.MessageComposerState
import cloud.orelit.communicationplatform.features.messages.impl.pinned.banner.PinnedMessagesBannerState
import cloud.orelit.communicationplatform.features.messages.impl.timeline.TimelineState
import cloud.orelit.communicationplatform.features.messages.impl.timeline.components.customreaction.CustomReactionState
import cloud.orelit.communicationplatform.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryState
import cloud.orelit.communicationplatform.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetState
import cloud.orelit.communicationplatform.features.messages.impl.timeline.protection.TimelineProtectionState
import cloud.orelit.communicationplatform.features.roomcall.api.RoomCallState
import cloud.orelit.communicationplatform.features.roommembermoderation.api.RoomMemberModerationState
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarMessage
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.encryption.identity.IdentityState
import cloud.orelit.communicationplatform.libraries.matrix.api.room.tombstone.SuccessorRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.user.DisplayedStatus
import kotlinx.collections.immutable.ImmutableList

data class MessagesState(
    val roomId: RoomId,
    val roomName: String?,
    val roomAvatar: AvatarData,
    val heroes: ImmutableList<AvatarData>,
    val userEventPermissions: UserEventPermissions,
    val composerState: MessageComposerState,
    val voiceMessageComposerState: VoiceMessageComposerState,
    val timelineState: TimelineState,
    val timelineProtectionState: TimelineProtectionState,
    val identityChangeState: IdentityChangeState,
    val linkState: LinkState,
    val actionListState: ActionListState,
    val customReactionState: CustomReactionState,
    val reactionSummaryState: ReactionSummaryState,
    val readReceiptBottomSheetState: ReadReceiptBottomSheetState,
    val snackbarMessage: SnackbarMessage?,
    val inviteProgress: AsyncData<Unit>,
    val showReinvitePrompt: Boolean,
    val enableTextFormatting: Boolean,
    val roomCallState: RoomCallState,
    val appName: String,
    val pinnedMessagesBannerState: PinnedMessagesBannerState,
    val dmUserVerificationState: IdentityState?,
    val dmUserStatus: DisplayedStatus?,
    val roomMemberModerationState: RoomMemberModerationState,
    /** Type of "shared history" icon to show in the top bar. */
    val topBarSharedHistoryIcon: SharedHistoryIcon,
    val successorRoom: SuccessorRoom?,
    val threads: Threads,
    val showLiveLocationShareBanner: Boolean,
    val eventSink: (MessagesEvent) -> Unit
) {
    val isTombstoned = successorRoom != null

    data class Threads(
        val hasThreads: Boolean,
        val hasUnreadThreads: Boolean,
    )
}

/** Type of "shared history" icon to show in the top bar. */
enum class SharedHistoryIcon {
    /** Show no icon at all. */
    NONE,

    /** history_visibility: shared. */
    SHARED,

    /** history_visibility: world_readable. */
    WORLD_READABLE
}
