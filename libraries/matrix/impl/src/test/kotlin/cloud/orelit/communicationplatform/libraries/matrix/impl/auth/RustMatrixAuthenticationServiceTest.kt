/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.auth

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.enterprise.api.EnterpriseService
import cloud.orelit.communicationplatform.features.enterprise.test.FakeEnterpriseService
import cloud.orelit.communicationplatform.libraries.featureflag.test.FakeFeatureFlagService
import cloud.orelit.communicationplatform.libraries.matrix.impl.ClientBuilderProvider
import cloud.orelit.communicationplatform.libraries.matrix.impl.FakeClientBuilderProvider
import cloud.orelit.communicationplatform.libraries.matrix.impl.createRustMatrixClientFactory
import cloud.orelit.communicationplatform.libraries.matrix.impl.fixtures.fakes.FakeFfiClient
import cloud.orelit.communicationplatform.libraries.matrix.impl.fixtures.fakes.FakeFfiClientBuilder
import cloud.orelit.communicationplatform.libraries.matrix.impl.fixtures.fakes.FakeFfiHomeserverLoginDetails
import cloud.orelit.communicationplatform.libraries.matrix.impl.paths.SessionPathsFactory
import cloud.orelit.communicationplatform.libraries.matrix.test.auth.FakeOAuthRedirectUrlProvider
import cloud.orelit.communicationplatform.libraries.matrix.test.core.aBuildMeta
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.SessionStore
import cloud.orelit.communicationplatform.libraries.sessionstorage.test.InMemorySessionStore
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

class RustMatrixAuthenticationServiceTest {
    @Test
    fun `setHomeserver is successful`() = runTest {
        val sut = createRustMatrixAuthenticationService(
            clientBuilderProvider = FakeClientBuilderProvider(
                provideResult = {
                    FakeFfiClientBuilder(
                        buildResult = {
                            FakeFfiClient(
                                homeserverLoginDetailsResult = {
                                    FakeFfiHomeserverLoginDetails()
                                }
                            )
                        }
                    )
                }
            ),
        )
        assertThat(sut.setHomeserver("matrix.org").isSuccess).isTrue()
    }

    @Test
    fun `setHomeserver can fail gracefully and clean up the temporary client`() = runTest {
        val closeResult = lambdaRecorder<Unit> {}
        val sut = createRustMatrixAuthenticationService(
            clientBuilderProvider = FakeClientBuilderProvider(
                provideResult = {
                    FakeFfiClientBuilder(
                        buildResult = {
                            FakeFfiClient(
                                homeserverLoginDetailsResult = {
                                    throw IllegalStateException("Failed to get homeserver login details")
                                },
                                closeResult = closeResult,
                            )
                        },
                    )
                },
            ),
        )
        assertThat(sut.setHomeserver("matrix.org").isFailure).isTrue()
        closeResult.assertions().isCalledOnce()
    }

    private fun TestScope.createRustMatrixAuthenticationService(
        sessionStore: SessionStore = InMemorySessionStore(),
        clientBuilderProvider: ClientBuilderProvider = FakeClientBuilderProvider(),
        enterpriseService: EnterpriseService = FakeEnterpriseService(),
    ): RustMatrixAuthenticationService {
        val baseDirectory = File("/base")
        val cacheDirectory = File("/cache")
        val rustMatrixClientFactory = createRustMatrixClientFactory(
            cacheDirectory = cacheDirectory,
            sessionStore = sessionStore,
            clientBuilderProvider = clientBuilderProvider,
        )
        return RustMatrixAuthenticationService(
            sessionPathsFactory = SessionPathsFactory(baseDirectory, cacheDirectory),
            coroutineDispatchers = testCoroutineDispatchers(),
            sessionStore = sessionStore,
            rustMatrixClientFactory = rustMatrixClientFactory,
            secretGenerator = FakeSecretGenerator(),
            oAuthConfigurationProvider = OAuthConfigurationProvider(
                buildMeta = aBuildMeta(),
                oAuthRedirectUrlProvider = FakeOAuthRedirectUrlProvider(),
            ),
            enterpriseService = enterpriseService,
            featureFlagService = FakeFeatureFlagService(),
            clientEnterpriseHook = {},
        )
    }
}
