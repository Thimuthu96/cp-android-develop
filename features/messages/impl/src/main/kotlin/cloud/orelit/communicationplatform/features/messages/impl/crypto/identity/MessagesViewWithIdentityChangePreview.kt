/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.crypto.identity

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import cloud.orelit.communicationplatform.features.messages.impl.MessagesView
import cloud.orelit.communicationplatform.features.messages.impl.aMessagesState
import cloud.orelit.communicationplatform.features.messages.impl.messagecomposer.aMessageComposerState
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.textcomposer.model.aTextEditorStateMarkdown

@PreviewsDayNight
@Composable
internal fun MessagesViewWithIdentityChangePreview(
    @PreviewParameter(IdentityChangeStatePreviewParam::class) identityChangeState: IdentityChangeState
) = ElementPreview {
    MessagesView(
        state = aMessagesState(
            composerState = aMessageComposerState(
                textEditorState = aTextEditorStateMarkdown(
                    initialText = "",
                    initialFocus = false,
                )
            ),
            identityChangeState = identityChangeState,
        ),
        onBackClick = {},
        onRoomDetailsClick = {},
        onEventContentClick = { _, _ -> false },
        onGalleryEventItemClick = { _, _, _ -> false },
        onUserDataClick = {},
        onLinkClick = { _, _ -> },
        onSendLocationClick = {},
        onCreatePollClick = {},
        onJoinCallClick = {},
        onViewAllPinnedMessagesClick = {},
        knockRequestsBannerView = {},
        customReactionBottomSheet = {},
        onThreadsListClick = {},
    )
}
