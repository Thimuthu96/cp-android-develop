/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.threads.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.ThreadId

@ContributesNode(RoomScope::class)
@AssistedInject
class ThreadsListNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: ThreadsListPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun openThread(threadId: ThreadId)
    }

    private val callback: Callback = callback()

    @Composable
    override fun View(modifier: Modifier) {
        ThreadsListView(
            state = presenter.present(),
            modifier = modifier,
            onThreadClick = callback::openThread,
            onBackClick = this::navigateUp,
        )
    }
}
