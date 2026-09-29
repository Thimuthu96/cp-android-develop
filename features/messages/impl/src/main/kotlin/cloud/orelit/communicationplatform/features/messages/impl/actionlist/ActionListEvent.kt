/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.actionlist

import cloud.orelit.communicationplatform.features.messages.impl.UserEventPermissions
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.TimelineItem

sealed interface ActionListEvent {
    data object Clear : ActionListEvent
    data class ComputeForMessage(
        val event: TimelineItem.Event,
        val userEventPermissions: UserEventPermissions,
    ) : ActionListEvent
}
