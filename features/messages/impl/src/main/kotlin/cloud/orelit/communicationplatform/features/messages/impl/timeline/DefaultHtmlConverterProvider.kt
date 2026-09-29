/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import cloud.orelit.communicationplatform.features.messages.api.timeline.HtmlConverterProvider
import cloud.orelit.communicationplatform.libraries.core.bool.orFalse
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.textcomposer.ElementRichTextEditorStyle
import cloud.orelit.communicationplatform.libraries.textcomposer.mentions.MentionSpanProvider
import io.element.android.wysiwyg.compose.StyledHtmlConverter
import io.element.android.wysiwyg.display.MentionDisplayHandler
import io.element.android.wysiwyg.display.TextDisplay
import io.element.android.wysiwyg.utils.HtmlConverter
import uniffi.wysiwyg_composer.newMentionDetector

@ContributesBinding(RoomScope::class)
@SingleIn(RoomScope::class)
class DefaultHtmlConverterProvider(
    private val mentionSpanProvider: MentionSpanProvider,
) : HtmlConverterProvider {
    private val htmlConverter: MutableState<HtmlConverter?> = mutableStateOf(null)

    @Composable
    override fun Update() {
        val isInEditMode = LocalInspectionMode.current
        val mentionDetector = remember(isInEditMode) {
            if (isInEditMode) null else newMentionDetector()
        }

        val editorStyle = ElementRichTextEditorStyle.textStyle()
        val context = LocalContext.current

        htmlConverter.value = remember(editorStyle) {
            StyledHtmlConverter(
                context = context,
                mentionDisplayHandler = object : MentionDisplayHandler {
                    override fun resolveAtRoomMentionDisplay(): TextDisplay {
                        val mentionSpan = mentionSpanProvider.createEveryoneMentionSpan()
                        return TextDisplay.Custom(mentionSpan)
                    }

                    override fun resolveMentionDisplay(text: String, url: String): TextDisplay {
                        val mentionSpan = mentionSpanProvider.getMentionSpanFor(text, url)
                        return if (mentionSpan != null) {
                            TextDisplay.Custom(mentionSpan)
                        } else {
                            TextDisplay.Plain
                        }
                    }
                },
                isEditor = false,
                isMention = { _, url ->
                    mentionDetector?.isMention(url).orFalse()
                }
            ).apply {
                configureWith(editorStyle)
            }
        }
    }

    override fun provide(): HtmlConverter {
        return htmlConverter.value ?: error("HtmlConverter wasn't instantiated. Make sure to call HtmlConverterProvider.Update() first.")
    }
}
