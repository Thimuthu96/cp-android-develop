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
import cloud.orelit.communicationplatform.libraries.architecture.NodeInputs

interface ShowLocationEntryPoint : FeatureEntryPoint {
    data class Inputs(
        val mode: ShowLocationMode,
    ) : NodeInputs

    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        inputs: Inputs,
    ): Node
}
