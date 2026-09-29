/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.factories.event

import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemEventContent
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FailedToParseStateContent

@Inject
class TimelineItemContentFailedToParseStateFactory {
    @Suppress("UNUSED_PARAMETER")
    fun create(failedToParseState: FailedToParseStateContent): TimelineItemEventContent {
        return TimelineItemUnknownContent
    }
}
