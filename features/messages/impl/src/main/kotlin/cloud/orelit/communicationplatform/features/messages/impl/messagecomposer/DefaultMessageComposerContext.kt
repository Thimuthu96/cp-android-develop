/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.messagecomposer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import cloud.orelit.communicationplatform.features.messages.api.MessageComposerContext
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.textcomposer.model.MessageComposerMode

@SingleIn(RoomScope::class)
@ContributesBinding(RoomScope::class)
class DefaultMessageComposerContext : MessageComposerContext {
    override var composerMode: MessageComposerMode by mutableStateOf(MessageComposerMode.Normal)
        internal set
}
