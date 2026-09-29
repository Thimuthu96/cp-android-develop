/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.messagecomposer

import androidx.compose.runtime.Stable
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.textcomposer.mentions.ResolvedSuggestion
import cloud.orelit.communicationplatform.libraries.textcomposer.model.MessageComposerMode
import cloud.orelit.communicationplatform.libraries.textcomposer.model.TextEditorState
import io.element.android.wysiwyg.display.TextDisplay
import kotlinx.collections.immutable.ImmutableList

@Stable
data class MessageComposerState(
    val textEditorState: TextEditorState,
    val isFullScreen: Boolean,
    val mode: MessageComposerMode,
    val isInThreadTimeline: Boolean,
    val showAttachmentSourcePicker: Boolean,
    val showTextFormatting: Boolean,
    val canShareLocation: Boolean,
    val suggestions: ImmutableList<ResolvedSuggestion>,
    val resolveMentionDisplay: (String, String) -> TextDisplay,
    val resolveAtRoomMentionDisplay: () -> TextDisplay,
    val slashCommandAction: AsyncAction<Unit>,
    val eventSink: (MessageComposerEvent) -> Unit,
)
