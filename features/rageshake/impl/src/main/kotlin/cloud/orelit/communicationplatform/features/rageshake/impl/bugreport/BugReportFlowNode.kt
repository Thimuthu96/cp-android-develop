/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.rageshake.impl.bugreport

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.features.rageshake.api.bugreport.BugReportEntryPoint
import cloud.orelit.communicationplatform.features.viewfolder.api.ViewFolderEntryPoint
import cloud.orelit.communicationplatform.libraries.architecture.BackstackView
import cloud.orelit.communicationplatform.libraries.architecture.BaseFlowNode
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import kotlinx.parcelize.Parcelize

@ContributesNode(AppScope::class)
@AssistedInject
class BugReportFlowNode(
    @Assisted val buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val viewFolderEntryPoint: ViewFolderEntryPoint,
) : BaseFlowNode<BugReportFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    private val callback: BugReportEntryPoint.Callback = callback()

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data class ViewLogs(
            val rootPath: String,
        ) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                val callback = object : BugReportNode.Callback {
                    override fun onDone() {
                        callback.onDone()
                    }

                    override fun navigateToViewLogs(basePath: String) {
                        backstack.push(NavTarget.ViewLogs(rootPath = basePath))
                    }
                }
                createNode<BugReportNode>(buildContext, listOf(callback))
            }
            is NavTarget.ViewLogs -> {
                val callback = object : ViewFolderEntryPoint.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }
                }
                val params = ViewFolderEntryPoint.Params(
                    rootPath = navTarget.rootPath,
                )
                viewFolderEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = params,
                    callback = callback,
                )
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}
