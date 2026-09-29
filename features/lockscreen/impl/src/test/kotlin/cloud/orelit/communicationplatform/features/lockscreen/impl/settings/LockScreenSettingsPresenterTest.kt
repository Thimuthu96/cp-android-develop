/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.lockscreen.impl.settings

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.lockscreen.impl.LockScreenConfig
import cloud.orelit.communicationplatform.features.lockscreen.impl.biometric.BiometricAuthenticator
import cloud.orelit.communicationplatform.features.lockscreen.impl.biometric.BiometricAuthenticatorManager
import cloud.orelit.communicationplatform.features.lockscreen.impl.biometric.FakeBiometricAuthenticator
import cloud.orelit.communicationplatform.features.lockscreen.impl.biometric.FakeBiometricAuthenticatorManager
import cloud.orelit.communicationplatform.features.lockscreen.impl.fixtures.aLockScreenConfig
import cloud.orelit.communicationplatform.features.lockscreen.impl.fixtures.aPinCodeManager
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.storage.InMemoryLockScreenStore
import cloud.orelit.communicationplatform.features.lockscreen.impl.storage.LockScreenStore
import cloud.orelit.communicationplatform.tests.testutils.awaitLastSequentialItem
import cloud.orelit.communicationplatform.tests.testutils.consumeItemsUntilPredicate
import cloud.orelit.communicationplatform.tests.testutils.test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LockScreenSettingsPresenterTest {
    @Test
    fun `present - remove pin option is hidden when mandatory`() = runTest {
        val presenter = createLockScreenSettingsPresenter(lockScreenConfig = aLockScreenConfig(isPinMandatory = true))
        presenter.test {
            awaitItem().also { state ->
                assertThat(state.showRemovePinOption).isFalse()
            }
        }
    }

    @Test
    fun `present - remove pin flow`() = runTest {
        val presenter = createLockScreenSettingsPresenter()
        presenter.test {
            consumeItemsUntilPredicate { state ->
                state.showRemovePinOption
            }.last().also { state ->
                state.eventSink(LockScreenSettingsEvent.OnRemovePin)
            }
            awaitLastSequentialItem().also { state ->
                assertThat(state.showRemovePinConfirmation).isTrue()
                state.eventSink(LockScreenSettingsEvent.CancelRemovePin)
            }
            awaitLastSequentialItem().also { state ->
                assertThat(state.showRemovePinConfirmation).isFalse()
                state.eventSink(LockScreenSettingsEvent.OnRemovePin)
            }
            awaitLastSequentialItem().also { state ->
                assertThat(state.showRemovePinConfirmation).isTrue()
                state.eventSink(LockScreenSettingsEvent.ConfirmRemovePin)
            }
            consumeItemsUntilPredicate {
                it.showRemovePinOption.not()
            }.last().also { state ->
                assertThat(state.showRemovePinConfirmation).isFalse()
                assertThat(state.showRemovePinOption).isFalse()
            }
        }
    }

    @Test
    fun `present - show toggle biometric if device is secured`() = runTest {
        val fakeBiometricAuthenticatorManager = FakeBiometricAuthenticatorManager(
            isDeviceSecured = true,
        )
        val presenter = createLockScreenSettingsPresenter(
            biometricAuthenticatorManager = fakeBiometricAuthenticatorManager
        )
        presenter.test {
            skipItems(1)
            assertThat(awaitItem().showToggleBiometric).isTrue()
        }
    }

    @Test
    fun `present - enable biometric unlock success`() = runTest {
        val fakeBiometricAuthenticatorManager = FakeBiometricAuthenticatorManager(
            createBiometricAuthenticator = {
                FakeBiometricAuthenticator(authenticateLambda = { BiometricAuthenticator.AuthenticationResult.Success })
            }
        )
        val presenter = createLockScreenSettingsPresenter(
            biometricAuthenticatorManager = fakeBiometricAuthenticatorManager
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                state.eventSink(LockScreenSettingsEvent.ToggleBiometricAllowed)
            }
            awaitItem().also { state ->
                assertThat(state.isBiometricEnabled).isTrue()
            }
        }
    }

    @Test
    fun `present - enable biometric unlock failure`() = runTest {
        val fakeBiometricAuthenticatorManager = FakeBiometricAuthenticatorManager(
            createBiometricAuthenticator = {
                FakeBiometricAuthenticator(authenticateLambda = { BiometricAuthenticator.AuthenticationResult.Failure() })
            }
        )
        val presenter = createLockScreenSettingsPresenter(
            biometricAuthenticatorManager = fakeBiometricAuthenticatorManager
        )
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                state.eventSink(LockScreenSettingsEvent.ToggleBiometricAllowed)
            }
        }
    }

    @Test
    fun `present - disable biometric unlock`() = runTest {
        val fakeBiometricAuthenticatorManager = FakeBiometricAuthenticatorManager(
            createBiometricAuthenticator = {
                FakeBiometricAuthenticator(authenticateLambda = { BiometricAuthenticator.AuthenticationResult.Failure() })
            }
        )
        val lockScreenStore = InMemoryLockScreenStore()
        val presenter = createLockScreenSettingsPresenter(
            lockScreenStore = lockScreenStore,
            biometricAuthenticatorManager = fakeBiometricAuthenticatorManager
        )
        lockScreenStore.setIsBiometricUnlockAllowed(true)

        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.isBiometricEnabled).isTrue()
                state.eventSink(LockScreenSettingsEvent.ToggleBiometricAllowed)
            }
            awaitItem().also { state ->
                assertThat(state.isBiometricEnabled).isFalse()
            }
        }
    }

    private suspend fun TestScope.createLockScreenSettingsPresenter(
        lockScreenConfig: LockScreenConfig = aLockScreenConfig(),
        biometricAuthenticatorManager: BiometricAuthenticatorManager = FakeBiometricAuthenticatorManager(),
        lockScreenStore: LockScreenStore = InMemoryLockScreenStore(),
    ): LockScreenSettingsPresenter {
        val pinCodeManager = aPinCodeManager(lockScreenStore = lockScreenStore).apply {
            createPinCode("1234")
        }
        return LockScreenSettingsPresenter(
            lockScreenStore = lockScreenStore,
            pinCodeManager = pinCodeManager,
            coroutineScope = this,
            lockScreenConfig = lockScreenConfig,
            biometricAuthenticatorManager = biometricAuthenticatorManager,
        )
    }
}
