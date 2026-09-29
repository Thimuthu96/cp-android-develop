/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.messagecomposer

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.textcomposer.mentions.ResolvedSuggestion
import cloud.orelit.communicationplatform.libraries.textcomposer.model.MessageComposerMode
import cloud.orelit.communicationplatform.libraries.textcomposer.model.TextEditorState
import cloud.orelit.communicationplatform.libraries.textcomposer.model.aTextEditorStateRich
import io.element.android.wysiwyg.display.TextDisplay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

open class MessageComposerStatePreviewParam : PreviewParameterProvider<MessageComposerState> {
    override val values: Sequence<MessageComposerState>
        get() = sequenceOf(
            aMessageComposerState(),
        )
}

fun aMessageComposerState(
    textEditorState: TextEditorState = aTextEditorStateRich(),
    isFullScreen: Boolean = false,
    mode: MessageComposerMode = MessageComposerMode.Normal,
    isInThreadTimeline: Boolean = false,
    showTextFormatting: Boolean = false,
    showAttachmentSourcePicker: Boolean = false,
    canShareLocation: Boolean = true,
    suggestions: ImmutableList<ResolvedSuggestion> = persistentListOf(),
    slashCommandAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (MessageComposerEvent) -> Unit = {},
) = MessageComposerState(
    textEditorState = textEditorState,
    isFullScreen = isFullScreen,
    mode = mode,
    isInThreadTimeline = isInThreadTimeline,
    showTextFormatting = showTextFormatting,
    showAttachmentSourcePicker = showAttachmentSourcePicker,
    canShareLocation = canShareLocation,
    suggestions = suggestions,
    resolveMentionDisplay = { _, _ -> TextDisplay.Plain },
    resolveAtRoomMentionDisplay = { TextDisplay.Plain },
    slashCommandAction = slashCommandAction,
    eventSink = eventSink,
)
