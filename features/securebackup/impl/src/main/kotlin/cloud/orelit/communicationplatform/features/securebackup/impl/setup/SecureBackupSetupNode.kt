/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.securebackup.impl.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.features.securebackup.impl.R
import cloud.orelit.communicationplatform.libraries.architecture.NodeInputs
import cloud.orelit.communicationplatform.libraries.architecture.inputs
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarMessage
import cloud.orelit.communicationplatform.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class SecureBackupSetupNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: SecureBackupSetupPresenter.Factory,
    private val snackbarDispatcher: SnackbarDispatcher,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val isChangeRecoveryKeyUserStory: Boolean,
    ) : NodeInputs

    private val inputs = inputs<Inputs>()

    private val presenter = presenterFactory.create(inputs.isChangeRecoveryKeyUserStory)

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        SecureBackupSetupView(
            state = state,
            onSuccess = {
                postSuccessSnackbar()
                navigateUp()
            },
            onBackClick = ::navigateUp,
            modifier = modifier,
        )
    }

    private fun postSuccessSnackbar() {
        snackbarDispatcher.post(
            SnackbarMessage(
                messageResId = if (inputs.isChangeRecoveryKeyUserStory) {
                    R.string.screen_recovery_key_change_success
                } else {
                    R.string.screen_recovery_key_setup_success
                }
            )
        )
    }
}
