/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.linknewdevice.impl.screens.qrcode

import androidx.compose.runtime.Composable
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

/**
 * Max number of QrCode rotation.
 * When QrCode is rotated this number of time, a TimeOut error is emitted.
 * Set to 0 to disable QrCode rotation.
 */
private const val MAX_QR_CODE_ROTATION = 0

@ContributesNode(SessionScope::class)
@AssistedInject
class ShowQrCodeNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    showQrCodePresenterFactory: ShowQrCodePresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    class Inputs(
        val data: String,
    ) : NodeInputs

    interface Callback : Plugin {
        fun navigateBack()
    }

    private val inputs: Inputs = inputs<Inputs>()
    private val callback: Callback = callback()
    private val showQrCodePresenter: ShowQrCodePresenter = showQrCodePresenterFactory.create(
        initialData = inputs.data,
        maxQrCodeRotation = MAX_QR_CODE_ROTATION,
    )

    @Composable
    override fun View(modifier: Modifier) {
        val state = showQrCodePresenter.present()
        ShowQrCodeView(
            state = state,
            modifier = modifier,
            onBackClick = callback::navigateBack,
        )
    }
}
