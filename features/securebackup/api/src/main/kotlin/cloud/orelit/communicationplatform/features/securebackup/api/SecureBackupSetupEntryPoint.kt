/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.securebackup.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import cloud.orelit.communicationplatform.libraries.architecture.FeatureEntryPoint
import cloud.orelit.communicationplatform.libraries.architecture.NodeInputs

interface SecureBackupSetupEntryPoint : FeatureEntryPoint {
    data class Inputs(val isChangeRecoveryKeyUserStory: Boolean) : NodeInputs

    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        inputs: Inputs,
    ): Node
}
