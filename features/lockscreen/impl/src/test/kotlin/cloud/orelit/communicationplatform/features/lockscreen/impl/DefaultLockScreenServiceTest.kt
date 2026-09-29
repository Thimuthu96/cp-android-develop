/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.lockscreen.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.lockscreen.impl.biometric.BiometricAuthenticatorManager
import cloud.orelit.communicationplatform.features.lockscreen.impl.biometric.FakeBiometricAuthenticatorManager
import cloud.orelit.communicationplatform.features.lockscreen.impl.fixtures.aLockScreenConfig
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.PinCodeManager
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.SECRET_KEY_ALIAS
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.createDefaultPinCodeManager
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.storage.InMemoryLockScreenStore
import cloud.orelit.communicationplatform.features.lockscreen.impl.storage.LockScreenStore
import cloud.orelit.communicationplatform.libraries.cryptography.api.SecretKeyRepository
import cloud.orelit.communicationplatform.libraries.cryptography.test.SimpleSecretKeyRepository
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.observer.SessionObserver
import cloud.orelit.communicationplatform.libraries.sessionstorage.test.observer.FakeSessionObserver
import cloud.orelit.communicationplatform.services.appnavstate.api.AppForegroundStateService
import cloud.orelit.communicationplatform.services.appnavstate.test.FakeAppForegroundStateService
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultLockScreenServiceTest {
    @Test
    fun `when the pin is not mandatory and no pin is configured isSetupRequired emits false`() = runTest {
        val sut = createDefaultLockScreenService(
            lockScreenConfig = aLockScreenConfig(isPinMandatory = false)
        )
        sut.isSetupRequired().test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `when the pin is mandatory, isSetupRequired emits true`() = runTest {
        val secretKeyRepository = SimpleSecretKeyRepository()
        val sut = createDefaultLockScreenService(
            lockScreenConfig = aLockScreenConfig(isPinMandatory = true),
            secretKeyRepository = secretKeyRepository,
        )
        sut.isSetupRequired().test {
            assertThat(awaitItem()).isTrue()
            // When the user configures the pin code, the setup is not required anymore
            secretKeyRepository.getOrCreateKey(SECRET_KEY_ALIAS, true)
            assertThat(awaitItem()).isFalse()
            // Users deletes the pin code
            secretKeyRepository.deleteKey("elementx.SECRET_KEY_ALIAS_PIN_CODE")
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `when the last session is deleted, the pin code is removed`() = runTest {
        val sessionObserver = FakeSessionObserver()
        val secretKeyRepository = SimpleSecretKeyRepository()
        val sut = createDefaultLockScreenService(
            lockScreenConfig = aLockScreenConfig(isPinMandatory = true),
            secretKeyRepository = secretKeyRepository,
            sessionObserver = sessionObserver,
        )
        sut.isPinSetup().test {
            assertThat(awaitItem()).isFalse()
            // When the user configure the pin code, the setup is not required anymore
            secretKeyRepository.getOrCreateKey(SECRET_KEY_ALIAS, true)
            assertThat(awaitItem()).isTrue()
            sessionObserver.onSessionDeleted("userId", wasLastSession = false)
            expectNoEvents()
            sessionObserver.onSessionDeleted("userId", wasLastSession = true)
            assertThat(awaitItem()).isFalse()
        }
    }
}

private fun TestScope.createDefaultLockScreenService(
    lockScreenConfig: LockScreenConfig = aLockScreenConfig(),
    lockScreenStore: LockScreenStore = InMemoryLockScreenStore(),
    secretKeyRepository: SecretKeyRepository = SimpleSecretKeyRepository(),
    pinCodeManager: PinCodeManager = createDefaultPinCodeManager(
        lockScreenStore = lockScreenStore,
        secretKeyRepository = secretKeyRepository,
    ),
    sessionObserver: SessionObserver = FakeSessionObserver(),
    appForegroundStateService: AppForegroundStateService = FakeAppForegroundStateService(),
    biometricAuthenticatorManager: BiometricAuthenticatorManager = FakeBiometricAuthenticatorManager(),
) = DefaultLockScreenService(
    lockScreenConfig = lockScreenConfig,
    lockScreenStore = lockScreenStore,
    pinCodeManager = pinCodeManager,
    coroutineScope = backgroundScope,
    sessionObserver = sessionObserver,
    appForegroundStateService = appForegroundStateService,
    biometricAuthenticatorManager = biometricAuthenticatorManager,
)
