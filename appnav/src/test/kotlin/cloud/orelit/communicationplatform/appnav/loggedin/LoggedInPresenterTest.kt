/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package cloud.orelit.communicationplatform.appnav.loggedin

import app.cash.turbine.ReceiveTurbine
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.CryptoSessionStateChange
import im.vector.app.features.analytics.plan.UserProperties
import cloud.orelit.communicationplatform.features.networkmonitor.api.NetworkStatus
import cloud.orelit.communicationplatform.features.networkmonitor.test.FakeNetworkMonitor
import cloud.orelit.communicationplatform.libraries.core.meta.BuildMeta
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.api.encryption.EncryptionService
import cloud.orelit.communicationplatform.libraries.matrix.api.encryption.RecoveryState
import cloud.orelit.communicationplatform.libraries.matrix.api.oauth.AccountManagementAction
import cloud.orelit.communicationplatform.libraries.matrix.api.roomlist.RoomListService
import cloud.orelit.communicationplatform.libraries.matrix.api.sync.SlidingSyncVersion
import cloud.orelit.communicationplatform.libraries.matrix.api.sync.SyncState
import cloud.orelit.communicationplatform.libraries.matrix.api.verification.SessionVerificationService
import cloud.orelit.communicationplatform.libraries.matrix.api.verification.SessionVerifiedStatus
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EXCEPTION
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SESSION_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeHomeserverCapabilitiesProvider
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.test.core.aBuildMeta
import cloud.orelit.communicationplatform.libraries.matrix.test.encryption.FakeEncryptionService
import cloud.orelit.communicationplatform.libraries.matrix.test.roomlist.FakeRoomListService
import cloud.orelit.communicationplatform.libraries.matrix.test.sync.FakeSyncService
import cloud.orelit.communicationplatform.libraries.matrix.test.verification.FakeSessionVerificationService
import cloud.orelit.communicationplatform.libraries.permissions.test.FakeLocalNetworkPermissionAdvisor
import cloud.orelit.communicationplatform.libraries.permissions.test.FakePermissionsPresenterFactory
import cloud.orelit.communicationplatform.libraries.push.api.PushService
import cloud.orelit.communicationplatform.libraries.push.api.PusherRegistrationFailure
import cloud.orelit.communicationplatform.libraries.push.test.FakePushService
import cloud.orelit.communicationplatform.libraries.pushproviders.api.Distributor
import cloud.orelit.communicationplatform.libraries.pushproviders.api.PushProvider
import cloud.orelit.communicationplatform.libraries.pushproviders.test.FakePushProvider
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService
import cloud.orelit.communicationplatform.services.analytics.test.FakeAnalyticsService
import cloud.orelit.communicationplatform.tests.testutils.WarmUpRule
import cloud.orelit.communicationplatform.tests.testutils.consumeItemsUntilPredicate
import cloud.orelit.communicationplatform.tests.testutils.lambda.assert
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.lambda.value
import cloud.orelit.communicationplatform.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class LoggedInPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        createLoggedInPresenter().test {
            val initialState = awaitItem()
            assertThat(initialState.syncIndicatorState).isEqualTo(SyncIndicatorState.Hidden)
            assertThat(initialState.pusherRegistrationState.isUninitialized()).isTrue()
            assertThat(initialState.ignoreRegistrationError).isFalse()
        }
    }

    @Test
    fun `present - ensure that account url is preloaded`() = runTest {
        val accountManagementUrlResult = lambdaRecorder<AccountManagementAction?, Result<String?>> { Result.success("aUrl") }
        val matrixClient = FakeMatrixClient(
            accountManagementUrlResult = accountManagementUrlResult,
        )
        createLoggedInPresenter(
            matrixClient = matrixClient,
        ).test {
            awaitItem()
            advanceUntilIdle()
            accountManagementUrlResult.assertions().isCalledOnce()
                .with(value(null))
        }
    }

    @Test
    fun `present - show sync spinner`() = runTest {
        val roomListService = FakeRoomListService()
        createLoggedInPresenter(
            syncState = SyncState.Running,
            matrixClient = FakeMatrixClient(roomListService = roomListService),
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.syncIndicatorState).isEqualTo(SyncIndicatorState.Hidden)
            roomListService.postSyncIndicator(RoomListService.SyncIndicator.Show)
            consumeItemsUntilPredicate { it.syncIndicatorState == SyncIndicatorState.Syncing }
            roomListService.postSyncIndicator(RoomListService.SyncIndicator.Hide)
            consumeItemsUntilPredicate { it.syncIndicatorState != SyncIndicatorState.Syncing }
        }
    }

    @Test
    fun `present - say the server is unreachable when the device has network but the sync is offline`() = runTest {
        createLoggedInPresenter(
            syncState = SyncState.Offline,
            networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Connected),
        ).test {
            consumeItemsUntilPredicate { it.syncIndicatorState == SyncIndicatorState.ServerUnreachable }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `present - say nothing when the device itself has no network`() = runTest {
        createLoggedInPresenter(
            syncState = SyncState.Offline,
            networkMonitor = FakeNetworkMonitor(initialStatus = NetworkStatus.Disconnected),
        ).test {
            consumeItemsUntilPredicate { it.syncIndicatorState == SyncIndicatorState.Hidden }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `present - report crypto status analytics`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val roomListService = FakeRoomListService()
        val verificationService = FakeSessionVerificationService()
        val encryptionService = FakeEncryptionService()
        val buildMeta = aBuildMeta()
        val networkMonitor = FakeNetworkMonitor()
        LoggedInPresenter(
            matrixClient = FakeMatrixClient(
                roomListService = roomListService,
                encryptionService = encryptionService,
            ),
            syncService = FakeSyncService(initialSyncState = SyncState.Running),
            pushService = FakePushService(
                ensurePusherIsRegisteredResult = { Result.success(Unit) },
            ),
            sessionVerificationService = verificationService,
            analyticsService = analyticsService,
            encryptionService = encryptionService,
            buildMeta = buildMeta,
            networkMonitor = networkMonitor,
            localNetworkPermissionAdvisor = FakeLocalNetworkPermissionAdvisor(),
            permissionsPresenterFactory = FakePermissionsPresenterFactory(),
        ).test {
            encryptionService.emitRecoveryState(RecoveryState.UNKNOWN)
            encryptionService.emitRecoveryState(RecoveryState.INCOMPLETE)
            verificationService.emitVerifiedStatus(SessionVerifiedStatus.Verified)
            skipItems(2)
            assertThat(analyticsService.capturedEvents.size).isEqualTo(1)
            assertThat(analyticsService.capturedEvents[0]).isInstanceOf(CryptoSessionStateChange::class.java)
            assertThat(analyticsService.capturedUserProperties.size).isEqualTo(1)
            assertThat(analyticsService.capturedUserProperties[0].recoveryState).isEqualTo(UserProperties.RecoveryState.Incomplete)
            assertThat(analyticsService.capturedUserProperties[0].verificationState).isEqualTo(UserProperties.VerificationState.Verified)
            // ensure a sync status change does not trigger a new capture
            roomListService.postSyncIndicator(RoomListService.SyncIndicator.Show)
            skipItems(1)
            assertThat(analyticsService.capturedEvents.size).isEqualTo(1)
        }
    }

    @Test
    fun `present - ensure default pusher is not registered if session is not verified`() = runTest {
        val lambda = lambdaRecorder<Result<Unit>> {
            Result.success(Unit)
        }
        val pushService = createFakePushService(ensurePusherIsRegisteredResult = lambda)
        val verificationService = FakeSessionVerificationService(
            initialSessionVerifiedStatus = SessionVerifiedStatus.NotVerified
        )
        createLoggedInPresenter(
            pushService = pushService,
            sessionVerificationService = verificationService,
        ).test {
            val finalState = awaitFirstItem()
            assertThat(finalState.pusherRegistrationState.errorOrNull())
                .isInstanceOf(PusherRegistrationFailure.AccountNotVerified::class.java)
            lambda.assertions().isNeverCalled()
        }
    }

    @Test
    fun `present - ensure default pusher is registered with default provider`() = runTest {
        val lambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val sessionVerificationService = FakeSessionVerificationService(
            initialSessionVerifiedStatus = SessionVerifiedStatus.Verified
        )
        val pushService = createFakePushService(
            ensurePusherIsRegisteredResult = lambda,
        )
        createLoggedInPresenter(
            pushService = pushService,
            sessionVerificationService = sessionVerificationService,
            matrixClient = FakeMatrixClient(
                accountManagementUrlResult = { Result.success(null) },
            ),
        ).test {
            val finalState = awaitFirstItem()
            assertThat(finalState.pusherRegistrationState.isSuccess()).isTrue()
            lambda.assertions()
                .isCalledOnce()
        }
    }

    @Test
    fun `present - ensure default pusher is registered with default provider - fail to register`() = runTest {
        val lambda = lambdaRecorder<Result<Unit>> { Result.failure(AN_EXCEPTION) }
        val sessionVerificationService = FakeSessionVerificationService(
            initialSessionVerifiedStatus = SessionVerifiedStatus.Verified
        )
        val pushService = createFakePushService(
            ensurePusherIsRegisteredResult = lambda,
        )
        createLoggedInPresenter(
            pushService = pushService,
            sessionVerificationService = sessionVerificationService,
            matrixClient = FakeMatrixClient(
                accountManagementUrlResult = { Result.success(null) },
            ),
        ).test {
            val finalState = awaitFirstItem()
            assertThat(finalState.pusherRegistrationState.isFailure()).isTrue()
            lambda.assertions()
                .isCalledOnce()
            // Reset the error and do not show again
            finalState.eventSink(LoggedInEvent.CloseErrorDialog(doNotShowAgain = false))
            val lastState = awaitItem()
            assertThat(lastState.pusherRegistrationState.isUninitialized()).isTrue()
            assertThat(lastState.ignoreRegistrationError).isFalse()
        }
    }

    @Test
    fun `present - ensure default pusher is registered with default provider - fail to register - do not show again`() = runTest {
        val lambda = lambdaRecorder<Result<Unit>> { Result.failure(AN_EXCEPTION) }
        val setIgnoreRegistrationErrorLambda = lambdaRecorder<SessionId, Boolean, Unit> { _, _ -> }
        val sessionVerificationService = FakeSessionVerificationService(
            initialSessionVerifiedStatus = SessionVerifiedStatus.Verified
        )
        val pushService = createFakePushService(
            ensurePusherIsRegisteredResult = lambda,
            setIgnoreRegistrationErrorLambda = setIgnoreRegistrationErrorLambda,
        )
        createLoggedInPresenter(
            pushService = pushService,
            sessionVerificationService = sessionVerificationService,
            matrixClient = FakeMatrixClient(
                accountManagementUrlResult = { Result.success(null) },
            ),
        ).test {
            val finalState = awaitFirstItem()
            assertThat(finalState.pusherRegistrationState.isFailure()).isTrue()
            lambda.assertions()
                .isCalledOnce()
            // Reset the error and do not show again
            finalState.eventSink(LoggedInEvent.CloseErrorDialog(doNotShowAgain = true))
            skipItems(1)
            setIgnoreRegistrationErrorLambda.assertions()
                .isCalledOnce()
                .with(
                    // SessionId
                    value(A_SESSION_ID),
                    // Ignore
                    value(true),
                )
            val lastState = awaitItem()
            assertThat(lastState.pusherRegistrationState.isUninitialized()).isTrue()
            assertThat(lastState.ignoreRegistrationError).isTrue()
        }
    }

    private fun createFakePushService(
        pushProvider0: PushProvider? = FakePushProvider(
            index = 0,
            name = "aFakePushProvider0",
            distributors = listOf(Distributor("aDistributorValue0", "aDistributorName0")),
            currentDistributor = { null },
        ),
        pushProvider1: PushProvider? = FakePushProvider(
            index = 1,
            name = "aFakePushProvider1",
            distributors = listOf(Distributor("aDistributorValue1", "aDistributorName1")),
            currentDistributor = { null },
        ),
        ensurePusherIsRegisteredResult: () -> Result<Unit> = {
            Result.success(Unit)
        },
        selectPushProviderLambda: (SessionId, PushProvider) -> Unit = { _, _ -> lambdaError() },
        currentPushProvider: (SessionId) -> PushProvider? = { null },
        setIgnoreRegistrationErrorLambda: (SessionId, Boolean) -> Unit = { _, _ -> lambdaError() },
    ): PushService {
        return FakePushService(
            availablePushProviders = listOfNotNull(pushProvider0, pushProvider1),
            ensurePusherIsRegisteredResult = ensurePusherIsRegisteredResult,
            currentPushProvider = currentPushProvider,
            selectPushProviderLambda = selectPushProviderLambda,
            setIgnoreRegistrationErrorLambda = setIgnoreRegistrationErrorLambda,
        )
    }

    @Test
    fun `present - CheckSlidingSyncProxyAvailability forces the sliding sync migration under the right circumstances`() = runTest {
        // The migration will be forced if the user is not using the native sliding sync
        val matrixClient = FakeMatrixClient(
            currentSlidingSyncVersionLambda = { Result.success(SlidingSyncVersion.Proxy) },
        )
        createLoggedInPresenter(
            matrixClient = matrixClient,
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.forceNativeSlidingSyncMigration).isFalse()
            initialState.eventSink(LoggedInEvent.CheckSlidingSyncProxyAvailability)
            assertThat(awaitItem().forceNativeSlidingSyncMigration).isTrue()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - LogoutAndMigrateToNativeSlidingSync logs out the user`() = runTest {
        val logoutLambda = lambdaRecorder<Boolean, Boolean, Unit> { userInitiated, ignoreSdkError ->
            assertThat(userInitiated).isTrue()
            assertThat(ignoreSdkError).isTrue()
        }
        val matrixClient = FakeMatrixClient(
            accountManagementUrlResult = { Result.success(null) },
        ).apply {
            this.logoutLambda = logoutLambda
        }
        createLoggedInPresenter(
            matrixClient = matrixClient,
        ).test {
            val initialState = awaitItem()

            initialState.eventSink(LoggedInEvent.LogoutAndMigrateToNativeSlidingSync)

            advanceUntilIdle()

            assertThat(logoutLambda.assertions().isCalledOnce())
        }
    }

    @Test
    fun `present - refreshes homeserver capabilities when network is back`() = runTest {
        val refreshLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val matrixClient = FakeMatrixClient(
            homeserverCapabilitiesProvider = FakeHomeserverCapabilitiesProvider(refresh = refreshLambda),
            accountManagementUrlResult = { Result.success(null) },
        )
        val networkMonitor = FakeNetworkMonitor()
        createLoggedInPresenter(
            matrixClient = matrixClient,
            networkMonitor = networkMonitor,
        ).test {
            awaitItem()
            networkMonitor.connectivity.value = NetworkStatus.Connected

            advanceUntilIdle()

            refreshLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - does not enable automatic call status when server does not support it`() = runTest {
        val enableAutomaticCallStatusLambda = lambdaRecorder<Boolean, Unit> { }
        val matrixClient = FakeMatrixClient(
            accountManagementUrlResult = { Result.success(null) },
            enableAutomaticCallStatusLambda = enableAutomaticCallStatusLambda,
        ).apply {
            isUserStatusSupportedResult = Result.success(false)
        }
        createLoggedInPresenter(
            matrixClient = matrixClient,
        ).test {
            cancelAndConsumeRemainingEvents()
            assert(enableAutomaticCallStatusLambda)
                .isCalledOnce()
                .with(value(false))
        }
    }

    @Test
    fun `present - enables automatic call status when server supports it`() = runTest {
        val enableAutomaticCallStatusLambda = lambdaRecorder<Boolean, Unit> { }
        val matrixClient = FakeMatrixClient(
            accountManagementUrlResult = { Result.success(null) },
            enableAutomaticCallStatusLambda = enableAutomaticCallStatusLambda,
        ).apply {
            isUserStatusSupportedResult = Result.success(true)
        }
        createLoggedInPresenter(
            matrixClient = matrixClient,
        ).test {
            cancelAndConsumeRemainingEvents()
            assert(enableAutomaticCallStatusLambda)
                .isCalledOnce()
                .with(value(true))
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
        skipItems(1)
        return awaitItem()
    }

    private fun createLoggedInPresenter(
        syncState: SyncState = SyncState.Running,
        analyticsService: AnalyticsService = FakeAnalyticsService(),
        sessionVerificationService: SessionVerificationService = FakeSessionVerificationService(),
        encryptionService: EncryptionService = FakeEncryptionService(),
        pushService: PushService = FakePushService(),
        matrixClient: MatrixClient = FakeMatrixClient(
            accountManagementUrlResult = { Result.success(null) },
        ),
        buildMeta: BuildMeta = aBuildMeta(),
        networkMonitor: FakeNetworkMonitor = FakeNetworkMonitor(),
    ): LoggedInPresenter {
        return LoggedInPresenter(
            matrixClient = matrixClient,
            syncService = FakeSyncService(initialSyncState = syncState),
            pushService = pushService,
            sessionVerificationService = sessionVerificationService,
            analyticsService = analyticsService,
            encryptionService = encryptionService,
            buildMeta = buildMeta,
            networkMonitor = networkMonitor,
            localNetworkPermissionAdvisor = FakeLocalNetworkPermissionAdvisor(),
            permissionsPresenterFactory = FakePermissionsPresenterFactory(),
        )
    }
}
