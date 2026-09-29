/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.securebackup.impl.reset

import android.app.Activity
import android.os.Parcelable
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.features.enterprise.api.SessionEnterpriseService
import cloud.orelit.communicationplatform.features.securebackup.impl.reset.password.ResetIdentityPasswordNode
import cloud.orelit.communicationplatform.features.securebackup.impl.reset.root.ResetIdentityRootNode
import cloud.orelit.communicationplatform.libraries.androidutils.browser.openUrlInChromeCustomTab
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.architecture.BackstackView
import cloud.orelit.communicationplatform.libraries.architecture.BaseFlowNode
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import cloud.orelit.communicationplatform.libraries.designsystem.components.ProgressDialog
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.di.annotations.SessionCoroutineScope
import cloud.orelit.communicationplatform.libraries.matrix.api.encryption.IdentityOAuthResetHandle
import cloud.orelit.communicationplatform.libraries.matrix.api.encryption.IdentityPasswordResetHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@ContributesNode(SessionScope::class)
@AssistedInject
class ResetIdentityFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val resetIdentityFlowManager: ResetIdentityFlowManager,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val sessionEnterpriseService: SessionEnterpriseService,
) : BaseFlowNode<ResetIdentityFlowNode.NavTarget>(
    backstack = BackStack(initialElement = NavTarget.Root, savedStateMap = buildContext.savedStateMap),
    buildContext = buildContext,
    plugins = plugins,
) {
    interface Callback : Plugin {
        fun onDone()
    }

    private val callback: Callback = callback()

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data object ResetPassword : NavTarget
    }

    private lateinit var activity: Activity
    private var darkTheme: Boolean = false
    private var resetJob: Job? = null

    override fun onBuilt() {
        super.onBuilt()

        resetIdentityFlowManager.whenResetIsDone {
            callback.onDone()
        }
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.Root -> {
                val callback = object : ResetIdentityRootNode.Callback {
                    override fun onContinue() {
                        sessionCoroutineScope.startReset()
                    }
                }
                createNode<ResetIdentityRootNode>(buildContext, listOf(callback))
            }
            is NavTarget.ResetPassword -> {
                val handle = resetIdentityFlowManager.currentHandleFlow.value.dataOrNull() as? IdentityPasswordResetHandle ?: error("No password handle found")
                createNode<ResetIdentityPasswordNode>(
                    buildContext,
                    listOf(ResetIdentityPasswordNode.Inputs(handle))
                )
            }
        }
    }

    private fun CoroutineScope.startReset() = launch {
        // Instead of cancelling the reset job on every ON_START, we can do it before starting a new attempt
        cancelResetJob()

        val handleResult = resetIdentityFlowManager.getResetHandle()
            // We're only interested in the success/failure case, and we need this flow to stop by itself
            // since each call to `startReset` will create a new one
            .first { it.isSuccess() || it.isFailure() }

        when (handleResult) {
            is AsyncData.Failure -> {
                cancelResetJob()
                Timber.e(handleResult.error, "Could not load the reset identity handle.")
            }
            is AsyncData.Success -> {
                when (val handle = handleResult.data) {
                    null -> {
                        Timber.d("No reset handle return, the reset is done.")
                    }
                    is IdentityOAuthResetHandle -> {
                        Timber.d("Launching reset confirmation in MAS")
                        val url = sessionEnterpriseService.tweakMasUrl(handle.url)
                        activity.openUrlInChromeCustomTab(null, darkTheme, url)
                        Timber.d("Starting resetOAuth")
                        resetJob = launch { handle.resetOAuth() }
                        resetJob?.invokeOnCompletion { Timber.d("resetOAuth ended") }
                    }
                    is IdentityPasswordResetHandle -> backstack.push(NavTarget.ResetPassword)
                }
            }
            else -> Unit
        }
    }

    override fun performUpNavigation(): Boolean {
        val navigatesUp = super.performUpNavigation()

        // This intercepts the back navigation so we only cancel this job when the user actually navigates up
        if (navigatesUp) {
            sessionCoroutineScope.launch { resetIdentityFlowManager.cancel() }
            cancelResetJob()
        }

        return navigatesUp
    }

    private fun cancelResetJob() {
        resetJob?.cancel()
        resetJob = null
    }

    @Composable
    override fun View(modifier: Modifier) {
        // Workaround to get the current activity
        if (!this::activity.isInitialized) {
            activity = requireNotNull(LocalActivity.current)
        }
        darkTheme = !ElementTheme.isLightTheme
        val startResetState by resetIdentityFlowManager.currentHandleFlow.collectAsState()
        if (startResetState.isLoading()) {
            ProgressDialog(
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
                onDismissRequest = { sessionCoroutineScope.launch { cancelResetJob() } }
            )
        }

        BackstackView(modifier)
    }
}
