/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.utils

import cloud.orelit.communicationplatform.libraries.textcomposer.mentions.MentionSpanFormatter
import cloud.orelit.communicationplatform.libraries.textcomposer.mentions.MentionType

class FakeMentionSpanFormatter(
    private val formatLambda: (MentionType) -> CharSequence = { type -> type.toString() },
) : MentionSpanFormatter {
    override fun formatDisplayText(mentionType: MentionType): CharSequence {
        return formatLambda(mentionType)
    }
}
