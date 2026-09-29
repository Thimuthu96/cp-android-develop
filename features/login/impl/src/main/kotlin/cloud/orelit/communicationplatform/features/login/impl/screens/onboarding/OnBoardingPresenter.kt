/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.screens.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.appconfig.OnBoardingConfig
import cloud.orelit.communicationplatform.features.enterprise.api.EnterpriseService
import cloud.orelit.communicationplatform.features.enterprise.api.canConnectToAnyHomeserver
import cloud.orelit.communicationplatform.features.login.impl.accesscontrol.DefaultAccountProviderAccessControl
import cloud.orelit.communicationplatform.features.login.impl.accountprovider.AccountProviderDataSource
import cloud.orelit.communicationplatform.features.login.impl.login.LoginModeEvent
import cloud.orelit.communicationplatform.features.login.impl.login.LoginModeState
import cloud.orelit.communicationplatform.features.rageshake.api.RageshakeFeatureAvailability
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.core.meta.BuildMeta
import cloud.orelit.communicationplatform.libraries.core.meta.BuildType
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.SessionStore
import cloud.orelit.communicationplatform.libraries.ui.utils.MultipleTapToUnlock
import kotlinx.coroutines.launch

@AssistedInject
class OnBoardingPresenter(
    @Assisted private val params: OnBoardingNode.Params,
    private val buildMeta: BuildMeta,
    private val enterpriseService: EnterpriseService,
    private val defaultAccountProviderAccessControl: DefaultAccountProviderAccessControl,
    private val rageshakeFeatureAvailability: RageshakeFeatureAvailability,
    private val loginModePresenter: Presenter<LoginModeState>,
    private val onBoardingLogoResIdProvider: OnBoardingLogoResIdProvider,
    private val sessionStore: SessionStore,
    private val accountProviderDataSource: AccountProviderDataSource,
) : Presenter<OnBoardingState> {
    @AssistedFactory
    interface Factory {
        fun create(
            params: OnBoardingNode.Params,
        ): OnBoardingPresenter
    }

    private val multipleTapToUnlock = MultipleTapToUnlock()

    @Composable
    override fun present(): OnBoardingState {
        val localCoroutineScope = rememberCoroutineScope()
        val forcedAccountProvider = remember {
            // If homeserverAllowList() returns a singleton list, this is the default account provider.
            // In this case, the user can sign in using this homeserver, or use QrCode login
            enterpriseService.homeserverAllowList().singleOrNull()
        }
        val canConnectToAnyHomeserver = remember {
            enterpriseService.canConnectToAnyHomeserver()
        }
        val mustChooseAccountProvider = remember {
            !canConnectToAnyHomeserver && enterpriseService.homeserverAllowList().size > 1
        }
        val linkAccountProvider by produceState<String?>(initialValue = null) {
            // Account provider from the link, if allowed by the enterprise service
            value = params.accountProvider?.takeIf {
                try {
                    defaultAccountProviderAccessControl.assertIsAllowedToConnectToAccountProvider(it, it)
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
        val defaultAccountProvider = remember(linkAccountProvider) {
            // If there is a forced account provider, this is the default account provider
            // Else use the account provider passed in the params if any and if allowed
            forcedAccountProvider ?: linkAccountProvider
        }
        val canLoginWithQrCode by produceState(initialValue = false, linkAccountProvider) {
            value = linkAccountProvider == null
        }
        val canReportBug by remember { rageshakeFeatureAvailability.isAvailable() }.collectAsState(false)
        var showReportBug by rememberSaveable { mutableStateOf(false) }
        val onBoardingLogoResId = remember {
            onBoardingLogoResIdProvider.get()
        }
        val isAddingAccount by produceState(initialValue = false) {
            // We are adding an account if there is at least one session already stored
            value = sessionStore.numberOfSessions() > 0
        }

        val loginModeState = loginModePresenter.present()

        fun handleEvent(event: OnBoardingEvent) {
            when (event) {
                is OnBoardingEvent.OnSignIn -> localCoroutineScope.launch {
                    // Ensure that the current account provider is set
                    accountProviderDataSource.setUrl(event.defaultAccountProvider)
                    loginModeState.eventSink(
                        LoginModeEvent.Submit(
                            isAccountCreation = false,
                            homeserverUrl = event.defaultAccountProvider,
                            resolvedHomeserverUrl = null,
                            loginHint = params.loginHint?.takeIf { forcedAccountProvider == null },
                        )
                    )
                }
                OnBoardingEvent.ClearError -> loginModeState.eventSink(LoginModeEvent.ClearError)
                OnBoardingEvent.OnVersionClick -> {
                    if (canReportBug) {
                        if (multipleTapToUnlock.unlock(localCoroutineScope)) {
                            showReportBug = true
                        }
                    }
                }
            }
        }

        return OnBoardingState(
            isAddingAccount = isAddingAccount,
            showBackButton = params.showBackButton,
            showDeveloperSettings = buildMeta.buildType != BuildType.RELEASE,
            productionApplicationName = buildMeta.productionApplicationName,
            defaultAccountProvider = defaultAccountProvider,
            mustChooseAccountProvider = mustChooseAccountProvider,
            canLoginWithQrCode = canLoginWithQrCode,
            canCreateAccount = defaultAccountProvider == null && canConnectToAnyHomeserver && OnBoardingConfig.CAN_CREATE_ACCOUNT,
            canReportBug = canReportBug && showReportBug,
            loginModeState = loginModeState,
            version = buildMeta.versionName,
            onBoardingLogoResId = onBoardingLogoResId,
            eventSink = ::handleEvent,
        )
    }
}
