/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.screens.chooseaccountprovider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.appconfig.AuthenticationConfig
import cloud.orelit.communicationplatform.features.enterprise.api.EnterpriseService
import cloud.orelit.communicationplatform.features.login.impl.accountprovider.AccountProvider
import cloud.orelit.communicationplatform.features.login.impl.login.LoginModeEvent
import cloud.orelit.communicationplatform.features.login.impl.login.LoginModeState
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.core.uri.ensureProtocol
import kotlinx.collections.immutable.toImmutableList

@Inject
class ChooseAccountProviderPresenter(
    private val enterpriseService: EnterpriseService,
    private val loginModePresenter: Presenter<LoginModeState>,
) : Presenter<ChooseAccountProviderState> {
    @Composable
    override fun present(): ChooseAccountProviderState {
        val loginModeState = loginModePresenter.present()
        var selectedAccountProvider: AccountProvider? by remember { mutableStateOf(null) }

        fun handleEvent(event: ChooseAccountProviderEvent) {
            when (event) {
                ChooseAccountProviderEvent.Continue -> {
                    selectedAccountProvider?.let { provider ->
                        loginModeState.eventSink(
                            LoginModeEvent.Submit(
                                isAccountCreation = false,
                                homeserverUrl = provider.url,
                                resolvedHomeserverUrl = null,
                                loginHint = null,
                            )
                        )
                    }
                }
                is ChooseAccountProviderEvent.SelectAccountProvider -> {
                    // Ensure that the user does not change the server during processing.
                    if (loginModeState.loginMode is AsyncData.Uninitialized) {
                        selectedAccountProvider = event.accountProvider
                    }
                }
                ChooseAccountProviderEvent.ClearError -> loginModeState.eventSink(LoginModeEvent.ClearError)
            }
        }

        val staticAccountProviderList = remember {
            // The list cannot contains ANY_ACCOUNT_PROVIDER ("*") and cannot be empty at this point
            enterpriseService.homeserverAllowList()
                .map { it.ensureProtocol() }
                .map { url ->
                    AccountProvider(
                        url = url,
                        subtitle = null,
                        isPublic = url == AuthenticationConfig.MATRIX_ORG_URL,
                        isMatrixOrg = url == AuthenticationConfig.MATRIX_ORG_URL,
                    )
                }
                .toImmutableList()
        }

        return ChooseAccountProviderState(
            accountProviders = staticAccountProviderList,
            selectedAccountProvider = selectedAccountProvider,
            loginModeState = loginModeState,
            eventSink = ::handleEvent,
        )
    }
}
