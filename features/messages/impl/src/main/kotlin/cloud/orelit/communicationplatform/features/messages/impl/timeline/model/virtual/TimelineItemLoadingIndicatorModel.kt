/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.model.virtual

import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline

data class TimelineItemLoadingIndicatorModel(
    val direction: Timeline.PaginationDirection,
    val timestamp: Long,
) : TimelineItemVirtualModel {
    override val type: String = "TimelineItemLoadingIndicatorModel"
}
