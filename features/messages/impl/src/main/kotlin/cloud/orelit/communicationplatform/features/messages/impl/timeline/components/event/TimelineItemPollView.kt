/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.components.event

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import cloud.orelit.communicationplatform.features.messages.impl.timeline.TimelineEvent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemPollContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemPollContentPreviewParam
import cloud.orelit.communicationplatform.features.poll.api.pollcontent.PollContentView
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import kotlinx.collections.immutable.toImmutableList

@Composable
fun TimelineItemPollView(
    content: TimelineItemPollContent,
    eventSink: (TimelineEvent.TimelineItemPollEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onSelectAnswer(pollStartId: EventId, answerId: String) {
        eventSink(TimelineEvent.SelectPollAnswer(pollStartId, answerId))
    }

    fun onEndPoll(pollStartId: EventId) {
        eventSink(TimelineEvent.EndPoll(pollStartId))
    }

    fun onEditPoll(pollStartId: EventId) {
        eventSink(TimelineEvent.EditPoll(pollStartId))
    }

    PollContentView(
        eventId = content.eventId,
        question = content.question,
        answerItems = content.answerItems.toImmutableList(),
        pollKind = content.pollKind,
        isPollEnded = content.isEnded,
        isPollEditable = content.isEditable,
        isMine = content.isMine,
        onSelectAnswer = ::onSelectAnswer,
        onEditPoll = ::onEditPoll,
        onEndPoll = ::onEndPoll,
        modifier = modifier,
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemPollViewPreview(@PreviewParameter(TimelineItemPollContentPreviewParam::class) content: TimelineItemPollContent) =
    ElementPreview {
        TimelineItemPollView(
            content = content,
            eventSink = {},
        )
    }
