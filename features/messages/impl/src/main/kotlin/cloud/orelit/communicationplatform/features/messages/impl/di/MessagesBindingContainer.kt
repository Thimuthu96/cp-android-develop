/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import cloud.orelit.communicationplatform.features.messages.impl.crypto.identity.IdentityChangeState
import cloud.orelit.communicationplatform.features.messages.impl.crypto.identity.IdentityChangeStatePresenter
import cloud.orelit.communicationplatform.features.messages.impl.crypto.sendfailure.resolve.ResolveVerifiedUserSendFailurePresenter
import cloud.orelit.communicationplatform.features.messages.impl.crypto.sendfailure.resolve.ResolveVerifiedUserSendFailureState
import cloud.orelit.communicationplatform.features.messages.impl.link.LinkPresenter
import cloud.orelit.communicationplatform.features.messages.impl.link.LinkState
import cloud.orelit.communicationplatform.features.messages.impl.pinned.banner.PinnedMessagesBannerPresenter
import cloud.orelit.communicationplatform.features.messages.impl.pinned.banner.PinnedMessagesBannerState
import cloud.orelit.communicationplatform.features.messages.impl.timeline.components.customreaction.CustomReactionPresenter
import cloud.orelit.communicationplatform.features.messages.impl.timeline.components.customreaction.CustomReactionState
import cloud.orelit.communicationplatform.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryPresenter
import cloud.orelit.communicationplatform.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryState
import cloud.orelit.communicationplatform.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetPresenter
import cloud.orelit.communicationplatform.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetState
import cloud.orelit.communicationplatform.features.messages.impl.timeline.protection.TimelineProtectionPresenter
import cloud.orelit.communicationplatform.features.messages.impl.timeline.protection.TimelineProtectionState
import cloud.orelit.communicationplatform.features.messages.impl.typing.TypingNotificationPresenter
import cloud.orelit.communicationplatform.features.messages.impl.typing.TypingNotificationState
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.di.RoomScope

@ContributesTo(RoomScope::class)
@BindingContainer
interface MessagesBindingContainer {
    @Binds
    fun bindPinnedMessagesBannerPresenter(presenter: PinnedMessagesBannerPresenter): Presenter<PinnedMessagesBannerState>

    @Binds
    fun bindResolveVerifiedUserSendFailurePresenter(presenter: ResolveVerifiedUserSendFailurePresenter): Presenter<ResolveVerifiedUserSendFailureState>

    @Binds
    fun bindTypingNotificationPresenter(presenter: TypingNotificationPresenter): Presenter<TypingNotificationState>

    @Binds
    fun bindTimelineProtectionPresenter(presenter: TimelineProtectionPresenter): Presenter<TimelineProtectionState>

    @Binds
    fun bindLinkPresenter(presenter: LinkPresenter): Presenter<LinkState>

    @Binds
    fun bindCustomReactionPresenter(presenter: CustomReactionPresenter): Presenter<CustomReactionState>

    @Binds
    fun bindReactionSummaryPresenter(presenter: ReactionSummaryPresenter): Presenter<ReactionSummaryState>

    @Binds
    fun bindReadReceiptBottomSheetPresenter(presenter: ReadReceiptBottomSheetPresenter): Presenter<ReadReceiptBottomSheetState>

    @Binds
    fun bindIdentityChangeStatePresenter(presenter: IdentityChangeStatePresenter): Presenter<IdentityChangeState>
}
