/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.accountselect.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.libraries.accountselect.api.AccountSelectEntryPoint
import cloud.orelit.communicationplatform.libraries.architecture.callback

@ContributesNode(AppScope::class)
@AssistedInject
class AccountSelectNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: AccountSelectPresenter,
) : Node(buildContext, plugins = plugins) {
    private val callback: AccountSelectEntryPoint.Callback = callback()

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        AccountSelectView(
            state = state,
            onDismiss = callback::onCancel,
            onSelectAccount = callback::onAccountSelected,
            modifier = modifier,
        )
    }
}
