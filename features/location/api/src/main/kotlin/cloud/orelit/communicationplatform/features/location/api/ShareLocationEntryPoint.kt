/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.location.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import cloud.orelit.communicationplatform.libraries.architecture.FeatureEntryPoint
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline

/**
 * The "Share location" screen.
 *
 * Allows a user to share a location message within a room.
 */
interface ShareLocationEntryPoint : FeatureEntryPoint {
    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        timelineMode: Timeline.Mode,
    ): Node
}
