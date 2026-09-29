/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.actionlist

import androidx.compose.runtime.Immutable
import cloud.orelit.communicationplatform.features.messages.impl.actionlist.model.TimelineItemAction
import cloud.orelit.communicationplatform.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailure
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.TimelineItem
import kotlinx.collections.immutable.ImmutableList

data class ActionListState(
    val target: Target,
    val eventSink: (ActionListEvent) -> Unit,
) {
    @Immutable
    sealed interface Target {
        data object None : Target
        data class Loading(val event: TimelineItem.Event) : Target
        data class Success(
            val event: TimelineItem.Event,
            val sentTimeFull: String,
            val displayEmojiReactions: Boolean,
            val recentEmojis: ImmutableList<String>,
            val verifiedUserSendFailure: VerifiedUserSendFailure,
            val actions: ImmutableList<TimelineItemAction>,
        ) : Target
    }
}
