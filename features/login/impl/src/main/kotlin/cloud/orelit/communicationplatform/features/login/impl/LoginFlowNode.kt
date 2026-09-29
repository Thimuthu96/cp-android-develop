/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl

import android.app.Activity
import android.os.Parcelable
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.replace
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.features.login.api.LoginEntryPoint
import cloud.orelit.communicationplatform.features.login.impl.accountprovider.AccountProviderDataSource
import cloud.orelit.communicationplatform.features.login.impl.classic.ElementClassicConnection
import cloud.orelit.communicationplatform.features.login.impl.qrcode.QrCodeLoginFlowNode
import cloud.orelit.communicationplatform.features.login.impl.screens.chooseaccountprovider.ChooseAccountProviderNode
import cloud.orelit.communicationplatform.features.login.impl.screens.classic.ClassicFlowNode
import cloud.orelit.communicationplatform.features.login.impl.screens.confirmaccountprovider.ConfirmAccountProviderNode
import cloud.orelit.communicationplatform.features.login.impl.screens.loginpassword.LoginPasswordNode
import cloud.orelit.communicationplatform.features.login.impl.screens.onboarding.OnBoardingNode
import cloud.orelit.communicationplatform.features.preferences.api.PreferencesEntryPoint
import cloud.orelit.communicationplatform.libraries.androidutils.browser.openUrlInChromeCustomTab
import cloud.orelit.communicationplatform.libraries.architecture.BackstackView
import cloud.orelit.communicationplatform.libraries.architecture.BaseFlowNode
import cloud.orelit.communicationplatform.libraries.architecture.NodeInputs
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import cloud.orelit.communicationplatform.libraries.architecture.inputs
import cloud.orelit.communicationplatform.libraries.di.annotations.AppCoroutineScope
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.OAuthDetails
import cloud.orelit.communicationplatform.libraries.oauth.api.OAuthAction
import cloud.orelit.communicationplatform.libraries.oauth.api.OAuthActionFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@ContributesNode(AppScope::class)
@AssistedInject
class LoginFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val accountProviderDataSource: AccountProviderDataSource,
    private val oAuthActionFlow: OAuthActionFlow,
    @AppCoroutineScope
    private val appCoroutineScope: CoroutineScope,
    private val elementClassicConnection: ElementClassicConnection,
    private val preferencesEntryPoint: PreferencesEntryPoint,
) : BaseFlowNode<LoginFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.CheckClassicFlow,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    data class Params(
        val accountProvider: String?,
        val loginHint: String?,
    ) : NodeInputs

    private val callback: LoginEntryPoint.Callback = callback()
    private var activity: Activity? = null
    private var darkTheme: Boolean = false

    private var externalAppStarted = false

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onResume = {
                if (externalAppStarted) {
                    externalAppStarted = false
                    // Workaround to detect that the Custom Chrome Tab has been closed
                    // If there is no coming OidcAction (that would end this Node),
                    // consider that the user has cancelled the login
                    // by pressing back or by closing the Custom Chrome Tab.
                    lifecycleScope.launch {
                        delay(5000)
                        oAuthActionFlow.post(OAuthAction.GoBack(toUnblock = true))
                    }
                }
            }
        )
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object CheckClassicFlow : NavTarget

        @Parcelize
        data class OnBoarding(
            val showBackButton: Boolean,
        ) : NavTarget

        @Parcelize
        data object QrCode : NavTarget

        @Parcelize
        data object AppDeveloperSettings : NavTarget

        @Parcelize
        data class ConfirmAccountProvider(
            val isAccountCreation: Boolean,
        ) : NavTarget

        @Parcelize
        data object ChooseAccountProvider : NavTarget

        @Parcelize
        data class LoginPassword(
            val initialLogin: String = "",
        ) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.CheckClassicFlow -> {
                val callback = object : ClassicFlowNode.Callback {
                    override fun navigateToOnBoarding(allowBackNavigation: Boolean) {
                        if (allowBackNavigation) {
                            backstack.push(NavTarget.OnBoarding(showBackButton = true))
                        } else {
                            backstack.replace(NavTarget.OnBoarding(showBackButton = false))
                        }
                    }

                    override fun navigateToLoginPassword() {
                        backstack.push(NavTarget.LoginPassword())
                    }

                    override fun navigateToOAuth(oAuthDetails: OAuthDetails) {
                        navigateToMas(oAuthDetails)
                    }
                }
                createNode<ClassicFlowNode>(buildContext, listOf(callback))
            }
            is NavTarget.OnBoarding -> {
                val callback = object : OnBoardingNode.Callback {
                    override fun navigateToSignUpFlow() {
                        backstack.push(
                            NavTarget.ConfirmAccountProvider(isAccountCreation = true)
                        )
                    }

                    override fun navigateToSignInFlow(mustChooseAccountProvider: Boolean) {
                        backstack.push(
                            if (mustChooseAccountProvider) {
                                NavTarget.ChooseAccountProvider
                            } else {
                                NavTarget.ConfirmAccountProvider(isAccountCreation = false)
                            }
                        )
                    }

                    override fun navigateToQrCode() {
                        backstack.push(NavTarget.QrCode)
                    }

                    override fun navigateToBugReport() {
                        callback.navigateToBugReport()
                    }

                    override fun navigateToOAuth(oAuthDetails: OAuthDetails) {
                        navigateToMas(oAuthDetails)
                    }

                    override fun navigateToDeveloperSettings() {
                        backstack.push(NavTarget.AppDeveloperSettings)
                    }

                    override fun navigateToLoginPassword() {
                        backstack.push(NavTarget.LoginPassword())
                    }

                    override fun onDone() {
                        if (navTarget.showBackButton) {
                            backstack.pop()
                        } else {
                            callback.onDone()
                        }
                    }
                }
                val params = inputs<Params>()
                val inputs = OnBoardingNode.Params(
                    accountProvider = params.accountProvider,
                    loginHint = params.loginHint,
                    showBackButton = navTarget.showBackButton,
                )
                createNode<OnBoardingNode>(buildContext, listOf(callback, inputs))
            }
            NavTarget.AppDeveloperSettings -> {
                val callback = object : PreferencesEntryPoint.DeveloperSettingsCallback {
                    override fun onDone() {
                        backstack.pop()
                    }
                }
                preferencesEntryPoint.createAppDeveloperSettingsNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = callback,
                )
            }
            NavTarget.ChooseAccountProvider -> {
                val callback = object : ChooseAccountProviderNode.Callback {
                    override fun navigateToOAuth(oAuthDetails: OAuthDetails) {
                        navigateToMas(oAuthDetails)
                    }

                    override fun navigateToLoginPassword() {
                        backstack.push(NavTarget.LoginPassword())
                    }
                }
                createNode<ChooseAccountProviderNode>(buildContext, listOf(callback))
            }
            NavTarget.QrCode -> {
                val callback = object : QrCodeLoginFlowNode.Callback {
                    override fun navigateBack() {
                        backstack.pop()
                    }
                }
                createNode<QrCodeLoginFlowNode>(buildContext, listOf(callback))
            }
            is NavTarget.ConfirmAccountProvider -> {
                val inputs = ConfirmAccountProviderNode.Inputs(
                    isAccountCreation = navTarget.isAccountCreation,
                )
                val callback = object : ConfirmAccountProviderNode.Callback {
                    override fun navigateToOAuth(oAuthDetails: OAuthDetails) {
                        navigateToMas(oAuthDetails)
                    }

                    override fun navigateToLoginPassword() {
                        backstack.push(NavTarget.LoginPassword())
                    }
                }
                createNode<ConfirmAccountProviderNode>(buildContext, plugins = listOf(inputs, callback))
            }
            is NavTarget.LoginPassword -> {
                val inputs = LoginPasswordNode.Inputs(
                    initialLogin = navTarget.initialLogin,
                )
                createNode<LoginPasswordNode>(buildContext, plugins = listOf(inputs))
            }
        }
    }

    private fun navigateToMas(oAuthDetails: OAuthDetails) {
        activity?.let {
            externalAppStarted = true
            it.openUrlInChromeCustomTab(null, darkTheme, oAuthDetails.url)
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        activity = requireNotNull(LocalActivity.current)
        darkTheme = !ElementTheme.isLightTheme

        DisposableEffect(Unit) {
            elementClassicConnection.start()
            onDispose {
                elementClassicConnection.stop()
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                activity = null
                appCoroutineScope.launch {
                    accountProviderDataSource.reset()
                }
            }
        }
        BackstackView(transitionHandler = rememberLoginFlowTransitionHandler())
    }
}
