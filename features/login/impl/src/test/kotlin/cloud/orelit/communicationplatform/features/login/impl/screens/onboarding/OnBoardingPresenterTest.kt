/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.screens.onboarding

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.appconfig.AuthenticationConfig
import cloud.orelit.communicationplatform.appconfig.OnBoardingConfig
import cloud.orelit.communicationplatform.features.enterprise.api.EnterpriseService
import cloud.orelit.communicationplatform.features.enterprise.api.IsEnterpriseBuild
import cloud.orelit.communicationplatform.features.enterprise.test.FakeEnterpriseService
import cloud.orelit.communicationplatform.features.login.impl.accesscontrol.DefaultAccountProviderAccessControl
import cloud.orelit.communicationplatform.features.login.impl.accountprovider.AccountProviderDataSource
import cloud.orelit.communicationplatform.features.login.impl.accountprovider.SaveAccountProviderToHistory
import cloud.orelit.communicationplatform.features.login.impl.accountprovider.anAccountProviderDataSource
import cloud.orelit.communicationplatform.features.login.impl.localnetwork.LocalNetworkPermissionGate
import cloud.orelit.communicationplatform.features.login.impl.login.LoginModePresenter
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.core.meta.BuildMeta
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.MatrixAuthenticationService
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_ACCOUNT_PROVIDER
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_ACCOUNT_PROVIDER_2
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_ACCOUNT_PROVIDER_3
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EXCEPTION
import cloud.orelit.communicationplatform.libraries.matrix.test.A_HOMESERVER_URL
import cloud.orelit.communicationplatform.libraries.matrix.test.A_HOMESERVER_URL_2
import cloud.orelit.communicationplatform.libraries.matrix.test.A_LOGIN_HINT
import cloud.orelit.communicationplatform.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import cloud.orelit.communicationplatform.libraries.matrix.test.core.aBuildMeta
import cloud.orelit.communicationplatform.libraries.oauth.api.OAuthActionFlow
import cloud.orelit.communicationplatform.libraries.oauth.test.FakeOAuthActionFlow
import cloud.orelit.communicationplatform.libraries.permissions.api.PermissionsPresenter
import cloud.orelit.communicationplatform.libraries.permissions.api.localnetwork.LocalNetworkPermissionAdvisor
import cloud.orelit.communicationplatform.libraries.permissions.test.FakeLocalNetworkPermissionAdvisor
import cloud.orelit.communicationplatform.libraries.permissions.test.FakePermissionsPresenterFactory
import cloud.orelit.communicationplatform.libraries.preferences.test.InMemoryAppPreferencesStore
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.SessionStore
import cloud.orelit.communicationplatform.libraries.sessionstorage.test.InMemorySessionStore
import cloud.orelit.communicationplatform.libraries.sessionstorage.test.aSessionData
import cloud.orelit.communicationplatform.tests.testutils.WarmUpRule
import cloud.orelit.communicationplatform.tests.testutils.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class OnBoardingPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    companion object {
        private const val ACCOUNT_PROVIDER_FROM_LINK = AN_ACCOUNT_PROVIDER
        private const val ACCOUNT_PROVIDER_FROM_CONFIG = AN_ACCOUNT_PROVIDER_2
        private const val ACCOUNT_PROVIDER_FROM_CONFIG_2 = AN_ACCOUNT_PROVIDER_3
    }

    @Test
    fun `present - ensure initial conditions`() {
        assertThat(
            setOf(
                ACCOUNT_PROVIDER_FROM_LINK,
                ACCOUNT_PROVIDER_FROM_CONFIG,
                ACCOUNT_PROVIDER_FROM_CONFIG_2,
            ).size
        ).isEqualTo(3)
    }

    @Test
    fun `present - initial state`() = runTest {
        val buildMeta = aBuildMeta(
            applicationName = "A",
            productionApplicationName = "B",
            desktopApplicationName = "C",
        )
        val presenter = createPresenter(
            buildMeta = buildMeta,
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = { listOf(ACCOUNT_PROVIDER_FROM_CONFIG, EnterpriseService.ANY_ACCOUNT_PROVIDER) },
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.showBackButton).isFalse()
            assertThat(initialState.defaultAccountProvider).isNull()
            assertThat(initialState.canLoginWithQrCode).isFalse()
            assertThat(initialState.productionApplicationName).isEqualTo("B")
            assertThat(initialState.canCreateAccount).isEqualTo(OnBoardingConfig.CAN_CREATE_ACCOUNT)
            assertThat(initialState.canReportBug).isFalse()
            assertThat(initialState.isAddingAccount).isFalse()
            val finalState = awaitItem()
            assertThat(finalState.canLoginWithQrCode).isTrue()
        }
    }

    @Test
    fun `present - initial state with back button`() = runTest {
        val presenter = createPresenter(
            params = OnBoardingNode.Params(
                accountProvider = null,
                loginHint = null,
                showBackButton = true,
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.showBackButton).isTrue()
            skipItems(1)
        }
    }

    @Test
    fun `present - initial state adding account`() = runTest {
        val presenter = createPresenter(
            sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData()
                )
            )
        )
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isAddingAccount).isTrue()
        }
    }

    @Test
    fun `present - on boarding logo`() = runTest {
        val presenter = createPresenter(
            onBoardingLogoResIdProvider = OnBoardingLogoResIdProvider { 42 },
        )
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.onBoardingLogoResId).isEqualTo(42)
        }
    }

    @Test
    fun `present - clicking on version 7 times has no effect if rageshake not available`() = runTest {
        val presenter = createPresenter(
            rageshakeFeatureAvailability = { flowOf(false) },
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.canReportBug).isFalse()
                repeat(7) {
                    state.eventSink(OnBoardingEvent.OnVersionClick)
                }
            }
            expectNoEvents()
        }
    }

    @Test
    fun `present - clicking on version 7 times will reveal the report a problem button`() = runTest {
        val presenter = createPresenter()
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.canReportBug).isFalse()
                repeat(7) {
                    state.eventSink(OnBoardingEvent.OnVersionClick)
                }
            }
            assertThat(awaitItem().canReportBug).isTrue()
        }
    }

    @Test
    fun `present - opening the app using link with allowed account provider, and the app does not force account provider`() = runTest {
        val presenter = createPresenter(
            params = OnBoardingNode.Params(
                accountProvider = ACCOUNT_PROVIDER_FROM_LINK,
                loginHint = null,
                showBackButton = false,
            ),
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = { listOf(ACCOUNT_PROVIDER_FROM_CONFIG, EnterpriseService.ANY_ACCOUNT_PROVIDER) },
                isAllowedToConnectToHomeserverResult = { true },
                isElementProEnforcedResult = { false },
            ),
        )
        presenter.test {
            skipItems(3)
            awaitItem().also {
                assertThat(it.defaultAccountProvider).isEqualTo(ACCOUNT_PROVIDER_FROM_LINK)
                assertThat(it.canLoginWithQrCode).isFalse()
                assertThat(it.canCreateAccount).isFalse()
            }
        }
    }

    @Test
    fun `present - opening the app using link with not allowed account provider, and the app does not force account provider`() = runTest {
        val presenter = createPresenter(
            params = OnBoardingNode.Params(
                accountProvider = ACCOUNT_PROVIDER_FROM_LINK,
                loginHint = null,
                showBackButton = false,
            ),
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = { listOf(ACCOUNT_PROVIDER_FROM_CONFIG, ACCOUNT_PROVIDER_FROM_CONFIG_2) },
                isAllowedToConnectToHomeserverResult = { false },
            ),
        )
        presenter.test {
            skipItems(1)
            awaitItem().also {
                assertThat(it.defaultAccountProvider).isNull()
                assertThat(it.canLoginWithQrCode).isTrue()
                assertThat(it.canCreateAccount).isFalse()
            }
        }
    }

    @Test
    fun `present - opening the app using link, and the app forces account provider`() = runTest {
        val presenter = createPresenter(
            params = OnBoardingNode.Params(
                accountProvider = ACCOUNT_PROVIDER_FROM_LINK,
                loginHint = null,
                showBackButton = false,
            ),
            enterpriseService = FakeEnterpriseService(
                defaultHomeserverListResult = { listOf(ACCOUNT_PROVIDER_FROM_CONFIG) },
            )
        )
        presenter.test {
            skipItems(1)
            awaitItem().also {
                assertThat(it.defaultAccountProvider).isEqualTo(ACCOUNT_PROVIDER_FROM_CONFIG)
                assertThat(it.canLoginWithQrCode).isTrue()
                assertThat(it.canCreateAccount).isFalse()
            }
        }
    }

    @Test
    fun `present - default account provider - login and clear error`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.failure(AN_EXCEPTION)
            },
        )
        val accountProviderDataSource = anAccountProviderDataSource()
        val presenter = createPresenter(
            params = OnBoardingNode.Params(
                accountProvider = A_HOMESERVER_URL,
                loginHint = A_LOGIN_HINT,
                showBackButton = false,
            ),
            enterpriseService = FakeEnterpriseService(
                isAllowedToConnectToHomeserverResult = { true },
                isElementProEnforcedResult = { false },
            ),
            loginModePresenter = createLoginModePresenter(
                authenticationService = authenticationService,
            ),
            accountProviderDataSource = accountProviderDataSource,
        )
        presenter.test {
            skipItems(3)
            awaitItem().also {
                assertThat(it.defaultAccountProvider).isEqualTo(A_HOMESERVER_URL)
                assertThat(accountProviderDataSource.flow.first().url).isEqualTo(AuthenticationConfig.MATRIX_ORG_URL)
                it.eventSink(OnBoardingEvent.OnSignIn(A_HOMESERVER_URL_2))
                skipItems(1) // Loading
                // Account data source has been updated
                assertThat(accountProviderDataSource.flow.first().url).isEqualTo(A_HOMESERVER_URL_2)
                // Check an error was returned
                val submittedState = awaitItem()
                assertThat(submittedState.loginModeState.loginMode).isInstanceOf(AsyncData.Failure::class.java)

                // Assert the error is then cleared
                submittedState.eventSink(OnBoardingEvent.ClearError)
                val clearedState = awaitItem()
                assertThat(clearedState.loginModeState.loginMode).isEqualTo(AsyncData.Uninitialized)
            }
        }
    }
}

private fun createPresenter(
    params: OnBoardingNode.Params = OnBoardingNode.Params(
        accountProvider = null,
        loginHint = null,
        showBackButton = false,
    ),
    buildMeta: BuildMeta = aBuildMeta(),
    enterpriseService: EnterpriseService = FakeEnterpriseService(),
    isEnterpriseBuild: IsEnterpriseBuild = { false },
    rageshakeFeatureAvailability: () -> Flow<Boolean> = { flowOf(true) },
    loginModePresenter: LoginModePresenter = createLoginModePresenter(),
    onBoardingLogoResIdProvider: OnBoardingLogoResIdProvider = OnBoardingLogoResIdProvider { null },
    sessionStore: SessionStore = InMemorySessionStore(),
    accountProviderDataSource: AccountProviderDataSource = anAccountProviderDataSource(),
) = OnBoardingPresenter(
    params = params,
    buildMeta = buildMeta,
    enterpriseService = enterpriseService,
    defaultAccountProviderAccessControl = DefaultAccountProviderAccessControl(
        enterpriseService = enterpriseService,
        isEnterpriseBuild = isEnterpriseBuild,
    ),
    rageshakeFeatureAvailability = rageshakeFeatureAvailability,
    loginModePresenter = loginModePresenter,
    onBoardingLogoResIdProvider = onBoardingLogoResIdProvider,
    sessionStore = sessionStore,
    accountProviderDataSource = accountProviderDataSource,
)

fun createLoginModePresenter(
    oAuthActionFlow: OAuthActionFlow = FakeOAuthActionFlow(),
    authenticationService: MatrixAuthenticationService = FakeMatrixAuthenticationService(),
    localNetworkPermissionAdvisor: LocalNetworkPermissionAdvisor =
        FakeLocalNetworkPermissionAdvisor(),
    permissionsPresenterFactory: PermissionsPresenter.Factory =
        FakePermissionsPresenterFactory(),
    saveAccountProviderToHistory: SaveAccountProviderToHistory =
        SaveAccountProviderToHistory(anAccountProviderDataSource(), InMemoryAppPreferencesStore()),
): LoginModePresenter = LoginModePresenter(
    oAuthActionFlow = oAuthActionFlow,
    authenticationService = authenticationService,
    localNetworkPermissionGate = LocalNetworkPermissionGate(
        advisor = localNetworkPermissionAdvisor,
        permissionsPresenterFactory = permissionsPresenterFactory,
    ),
    saveAccountProviderToHistory = saveAccountProviderToHistory,
)
