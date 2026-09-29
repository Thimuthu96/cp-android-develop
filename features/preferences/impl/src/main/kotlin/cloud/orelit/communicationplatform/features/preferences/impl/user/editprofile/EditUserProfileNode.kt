/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.user.editprofile

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
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser

@ContributesNode(SessionScope::class)
@AssistedInject
class EditUserProfileNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: EditUserProfilePresenter.Factory,
) : Node(buildContext, plugins = plugins),
    EditUserProfileNavigator {
    data class Inputs(
        val matrixUser: MatrixUser
    ) : NodeInputs

    interface Callback : Plugin {
        fun onDone()
    }

    val matrixUser = inputs<Inputs>().matrixUser
    val callback: Callback = callback()
    val presenter = presenterFactory.create(
        matrixUser = matrixUser,
        navigator = this,
    )

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        EditUserProfileView(
            state = state,
            onEditProfileSuccess = ::close,
            modifier = modifier
        )
    }

    override fun close() = callback.onDone()
}
