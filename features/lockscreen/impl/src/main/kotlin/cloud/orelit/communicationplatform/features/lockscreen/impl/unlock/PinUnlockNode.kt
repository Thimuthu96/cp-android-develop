/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.lockscreen.impl.unlock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.libraries.architecture.NodeInputs
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.architecture.inputs
import cloud.orelit.communicationplatform.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class PinUnlockNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: PinUnlockPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onUnlock()

        // For feature unlock
        fun onCancel()
    }

    data class Inputs(
        val forDeviceUnlock: Boolean,
    ) : NodeInputs

    private val callback: Callback = callback()
    private val inputs: Inputs = inputs()

    private val presenter: PinUnlockPresenter = presenterFactory.create(inputs.forDeviceUnlock)

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        LaunchedEffect(state.isUnlocked) {
            if (state.isUnlocked) {
                callback.onUnlock()
            }
        }
        PinUnlockView(
            state = state,
            // UnlockNode is only used for in-app unlock, so we can safely set isInAppUnlock to true.
            // It's set to false in PinUnlockActivity.
            isInAppUnlock = true,
            onCancel = callback::onCancel,
            modifier = modifier
        )
    }
}
