/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.textcomposer.impl.mentions

import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkParser
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.permalink.FakePermalinkParser
import cloud.orelit.communicationplatform.libraries.textcomposer.mentions.MentionSpanFormatter
import cloud.orelit.communicationplatform.libraries.textcomposer.mentions.MentionSpanProvider
import cloud.orelit.communicationplatform.libraries.textcomposer.mentions.MentionSpanTheme
import cloud.orelit.communicationplatform.libraries.textcomposer.mentions.MentionType

fun aMentionSpanProvider(
    permalinkParser: PermalinkParser = FakePermalinkParser(),
    mentionSpanFormatter: MentionSpanFormatter = object : MentionSpanFormatter {
        override fun formatDisplayText(mentionType: MentionType): CharSequence {
            return mentionType.toString()
        }
    },
    mentionSpanTheme: MentionSpanTheme = MentionSpanTheme(A_USER_ID),
): MentionSpanProvider {
    return MentionSpanProvider(
        permalinkParser = permalinkParser,
        mentionSpanFormatter = mentionSpanFormatter,
        mentionSpanTheme = mentionSpanTheme,
    )
}
