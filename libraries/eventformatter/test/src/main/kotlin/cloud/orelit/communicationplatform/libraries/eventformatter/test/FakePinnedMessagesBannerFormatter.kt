/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.eventformatter.test

import cloud.orelit.communicationplatform.libraries.eventformatter.api.PinnedMessagesBannerFormatter
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EventTimelineItem

class FakePinnedMessagesBannerFormatter(
    val formatLambda: (event: EventTimelineItem) -> CharSequence
) : PinnedMessagesBannerFormatter {
    override fun format(event: EventTimelineItem): CharSequence {
        return formatLambda(event)
    }
}
