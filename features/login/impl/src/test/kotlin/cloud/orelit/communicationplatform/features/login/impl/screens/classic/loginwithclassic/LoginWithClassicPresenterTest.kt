/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.screens.classic.loginwithclassic

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.login.impl.accountprovider.AccountProviderDataSource
import cloud.orelit.communicationplatform.features.login.impl.accountprovider.anAccountProviderDataSource
import cloud.orelit.communicationplatform.features.login.impl.classic.ElementClassicConnection
import cloud.orelit.communicationplatform.features.login.impl.classic.ElementClassicConnectionState
import cloud.orelit.communicationplatform.features.login.impl.classic.FakeElementClassicConnection
import cloud.orelit.communicationplatform.features.login.impl.classic.ROOM_KEYS_VERSION
import cloud.orelit.communicationplatform.features.login.impl.classic.anElementClassicReady
import cloud.orelit.communicationplatform.features.login.impl.classic.anElementClassicSession
import cloud.orelit.communicationplatform.features.login.impl.login.LoginModePresenter
import cloud.orelit.communicationplatform.features.login.impl.screens.onboarding.createLoginModePresenter
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EXCEPTION
import cloud.orelit.communicationplatform.libraries.matrix.test.A_FAILURE_REASON
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SECRET
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_NAME
import cloud.orelit.communicationplatform.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import cloud.orelit.communicationplatform.libraries.matrix.test.core.aBuildMeta
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LoginWithClassicPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.isElementPro).isFalse()
            assertThat(initialState.userId).isEqualTo(A_USER_ID)
            assertThat(initialState.displayName).isNull()
            assertThat(initialState.avatar).isNull()
            assertThat(initialState.loginWithClassicAction.isUninitialized()).isTrue()
            assertThat(initialState.loginModeState.loginMode.isUninitialized()).isTrue()
        }
    }

    @Test
    fun `present - initial state - element Pro`() = runTest {
        val presenter = createPresenter(
            isEnterpriseBuild = true,
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.isElementPro).isTrue()
        }
    }

    @Test
    fun `present - start login with correct state - user can login`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.failure(AN_EXCEPTION)
            },
        )
        val elementClassicConnection = FakeElementClassicConnection(
            startResult = {},
        )
        val presenter = createPresenter(
            elementClassicConnection = elementClassicConnection,
            loginModePresenter = createLoginModePresenter(
                authenticationService = authenticationService,
            ),
        )
        presenter.test {
            skipItems(1)
            elementClassicConnection.emitState(
                anElementClassicReady(
                    elementClassicSession = anElementClassicSession(
                        userId = A_USER_ID,
                        secrets = A_SECRET,
                        roomKeysVersion = ROOM_KEYS_VERSION,
                        doesContainBackupKey = true,
                    ),
                    displayName = A_USER_NAME,
                )
            )
            val readyState = awaitItem()
            assertThat(readyState.userId).isEqualTo(A_USER_ID)
            assertThat(readyState.displayName).isEqualTo(A_USER_NAME)
            readyState.eventSink(LoginWithClassicEvent.Submit)
            val loadingState = awaitItem()
            assertThat(loadingState.loginWithClassicAction.isLoading()).isTrue()
            skipItems(1)
        }
    }

    @Test
    fun `present - start login with no secrets - user can login and will have to verify manually`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.failure(AN_EXCEPTION)
            },
        )
        val elementClassicConnection = FakeElementClassicConnection(
            startResult = {},
        )
        val presenter = createPresenter(
            elementClassicConnection = elementClassicConnection,
            loginModePresenter = createLoginModePresenter(
                authenticationService = authenticationService,
            ),
        )
        presenter.test {
            skipItems(1)
            elementClassicConnection.emitState(
                anElementClassicReady(
                    elementClassicSession = anElementClassicSession(
                        userId = A_USER_ID,
                        secrets = null,
                        roomKeysVersion = null,
                    ),
                    displayName = A_USER_NAME,
                )
            )
            val readyState = awaitItem()
            assertThat(readyState.userId).isEqualTo(A_USER_ID)
            assertThat(readyState.displayName).isEqualTo(A_USER_NAME)
            readyState.eventSink(LoginWithClassicEvent.Submit)
            val loadingState = awaitItem()
            assertThat(loadingState.loginWithClassicAction.isLoading()).isTrue()
            skipItems(1)
        }
    }

    @Test
    fun `present - start login with secrets and without key backup - user will see the screen to enable key backup`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.failure(AN_EXCEPTION)
            },
        )
        val elementClassicConnection = FakeElementClassicConnection(
            startResult = {},
        )
        val navigateToMissingKeyBackupResult = lambdaRecorder<Unit> { }
        val presenter = createPresenter(
            elementClassicConnection = elementClassicConnection,
            loginModePresenter = createLoginModePresenter(
                authenticationService = authenticationService,
            ),
            navigator = FakeLoginWithClassicNavigator(
                navigateToMissingKeyBackupResult = navigateToMissingKeyBackupResult,
            ),
        )
        presenter.test {
            skipItems(1)
            elementClassicConnection.emitState(
                anElementClassicReady(
                    elementClassicSession = anElementClassicSession(
                        userId = A_USER_ID,
                        secrets = A_SECRET,
                        roomKeysVersion = null,
                        doesContainBackupKey = false,
                    ),
                    displayName = A_USER_NAME,
                )
            )
            val readyState = awaitItem()
            assertThat(readyState.userId).isEqualTo(A_USER_ID)
            assertThat(readyState.displayName).isEqualTo(A_USER_NAME)
            readyState.eventSink(LoginWithClassicEvent.Submit)
            navigateToMissingKeyBackupResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - start login with secrets and with invalid key backup - user will see the screen to enable key backup`() = runTest {
        val authenticationService = FakeMatrixAuthenticationService(
            setHomeserverResult = {
                Result.failure(AN_EXCEPTION)
            },
        )
        val elementClassicConnection = FakeElementClassicConnection(
            startResult = {},
        )
        val navigateToMissingKeyBackupResult = lambdaRecorder<Unit> { }
        val presenter = createPresenter(
            elementClassicConnection = elementClassicConnection,
            loginModePresenter = createLoginModePresenter(
                authenticationService = authenticationService,
            ),
            navigator = FakeLoginWithClassicNavigator(
                navigateToMissingKeyBackupResult = navigateToMissingKeyBackupResult,
            ),
        )
        presenter.test {
            skipItems(1)
            elementClassicConnection.emitState(
                anElementClassicReady(
                    elementClassicSession = anElementClassicSession(
                        userId = A_USER_ID,
                        secrets = A_SECRET,
                        roomKeysVersion = ROOM_KEYS_VERSION,
                        // false here
                        doesContainBackupKey = false,
                    ),
                    displayName = A_USER_NAME,
                )
            )
            val readyState = awaitItem()
            assertThat(readyState.userId).isEqualTo(A_USER_ID)
            assertThat(readyState.displayName).isEqualTo(A_USER_NAME)
            readyState.eventSink(LoginWithClassicEvent.Submit)
            navigateToMissingKeyBackupResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - submit in wrong state and clear error`() = runTest {
        val elementClassicConnection = FakeElementClassicConnection(
            startResult = {},
        )
        val presenter = createPresenter(
            elementClassicConnection = elementClassicConnection,
        )
        presenter.test {
            skipItems(1)
            elementClassicConnection.emitState(
                ElementClassicConnectionState.Error(
                    error = A_FAILURE_REASON,
                )
            )
            val initialState = awaitItem()
            assertThat(initialState.loginWithClassicAction.isUninitialized()).isTrue()
            initialState.eventSink(LoginWithClassicEvent.Submit)
            val errorState = awaitItem()
            assertThat(errorState.loginWithClassicAction.isFailure()).isTrue()
            errorState.eventSink(LoginWithClassicEvent.ClearError)
            val clearedState = awaitItem()
            assertThat(clearedState.loginWithClassicAction.isUninitialized()).isTrue()
        }
    }
}

private fun createPresenter(
    userId: UserId = A_USER_ID,
    navigator: LoginWithClassicNavigator = FakeLoginWithClassicNavigator(),
    loginModePresenter: LoginModePresenter = createLoginModePresenter(),
    elementClassicConnection: ElementClassicConnection = FakeElementClassicConnection(),
    accountProviderDataSource: AccountProviderDataSource = anAccountProviderDataSource(),
    isEnterpriseBuild: Boolean = false,
) = LoginWithClassicPresenter(
    userId = userId,
    navigator = navigator,
    loginModePresenter = loginModePresenter,
    elementClassicConnection = elementClassicConnection,
    accountProviderDataSource = accountProviderDataSource,
    buildMeta = aBuildMeta(
        isEnterpriseBuild = isEnterpriseBuild,
    ),
)
