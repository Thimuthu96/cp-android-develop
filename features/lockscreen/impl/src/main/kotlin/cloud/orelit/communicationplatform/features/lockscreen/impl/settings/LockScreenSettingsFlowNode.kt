/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.lockscreen.impl.settings

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.newRoot
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.DefaultPinCodeManagerCallback
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.PinCodeManager
import cloud.orelit.communicationplatform.features.lockscreen.impl.setup.pin.SetupPinNode
import cloud.orelit.communicationplatform.features.lockscreen.impl.unlock.PinUnlockNode
import cloud.orelit.communicationplatform.libraries.architecture.BackstackView
import cloud.orelit.communicationplatform.libraries.architecture.BaseFlowNode
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.ui.common.nodes.emptyNode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
@AssistedInject
class LockScreenSettingsFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val pinCodeManager: PinCodeManager,
) : BaseFlowNode<LockScreenSettingsFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Loading,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Loading : NavTarget

        @Parcelize
        data object Unlock : NavTarget

        @Parcelize
        data object SetupPin : NavTarget

        @Parcelize
        data object Settings : NavTarget
    }

    private val pinCodeManagerCallback = object : DefaultPinCodeManagerCallback() {
        override fun onPinCodeRemoved() {
            navigateUp()
        }

        override fun onPinCodeCreated() {
            backstack.newRoot(NavTarget.Settings)
        }
    }

    override fun onBuilt() {
        super.onBuilt()
        lifecycleScope.launch {
            val hasPinCode = pinCodeManager.hasPinCode().first()
            if (hasPinCode) {
                backstack.newRoot(NavTarget.Unlock)
            } else {
                backstack.newRoot(NavTarget.SetupPin)
            }
        }
        lifecycle.subscribe(
            onCreate = {
                pinCodeManager.addCallback(pinCodeManagerCallback)
            },
            onDestroy = {
                pinCodeManager.removeCallback(pinCodeManagerCallback)
            }
        )
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Loading -> {
                emptyNode(buildContext)
            }
            NavTarget.Unlock -> {
                val callback = object : PinUnlockNode.Callback {
                    override fun onUnlock() {
                        backstack.newRoot(NavTarget.Settings)
                    }

                    override fun onCancel() {
                        // Should not happen in this context.
                    }
                }
                val inputs = PinUnlockNode.Inputs(
                    forDeviceUnlock = false,
                )
                createNode<PinUnlockNode>(buildContext, plugins = listOf(callback, inputs))
            }
            NavTarget.SetupPin -> {
                createNode<SetupPinNode>(buildContext)
            }
            NavTarget.Settings -> {
                val callback = object : LockScreenSettingsNode.Callback {
                    override fun navigateToSetupPin() {
                        backstack.push(NavTarget.SetupPin)
                    }
                }
                createNode<LockScreenSettingsNode>(buildContext, plugins = listOf(callback))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}
