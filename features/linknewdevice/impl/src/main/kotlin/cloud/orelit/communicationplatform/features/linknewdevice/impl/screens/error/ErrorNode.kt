/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.linknewdevice.impl.screens.error

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.architecture.inputs
import cloud.orelit.communicationplatform.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class ErrorNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : Node(buildContext = buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onRetry()
        fun onCancel()
    }

    private val callback: Callback = callback()
    private val errorScreenType = inputs<ErrorScreenType>()

    @Composable
    override fun View(modifier: Modifier) {
        ErrorView(
            modifier = modifier,
            errorScreenType = errorScreenType,
            onRetry = callback::onRetry,
            onCancel = callback::onCancel,
        )
    }
}
