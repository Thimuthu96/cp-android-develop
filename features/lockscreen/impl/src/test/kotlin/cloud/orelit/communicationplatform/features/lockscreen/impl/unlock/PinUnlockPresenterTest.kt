/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.lockscreen.impl.unlock

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.lockscreen.impl.biometric.BiometricAuthenticatorManager
import cloud.orelit.communicationplatform.features.lockscreen.impl.biometric.FakeBiometricAuthenticatorManager
import cloud.orelit.communicationplatform.features.lockscreen.impl.fixtures.aPinCodeManager
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.DefaultPinCodeManagerCallback
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.PinCodeManager
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.model.PinEntry
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.model.assertText
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.storage.InMemoryLockScreenStore
import cloud.orelit.communicationplatform.features.lockscreen.impl.unlock.keypad.PinKeypadModel
import cloud.orelit.communicationplatform.features.logout.test.FakeLogoutUseCase
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.tests.testutils.lambda.assert
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PinUnlockPresenterTest {
    private val halfCompletePin = "12"
    private val completePin = "1235"

    @Test
    fun `present - success verify flow`() = runTest {
        val presenter = createPinUnlockPresenter()
        presenter.test {
            awaitItem().also { state ->
                assertThat(state.pinEntry).isInstanceOf(AsyncData.Uninitialized::class.java)
                assertThat(state.showWrongPinTitle).isFalse()
                assertThat(state.showSignOutPrompt).isFalse()
                assertThat(state.isUnlocked).isFalse()
                assertThat(state.signOutAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
                assertThat(state.remainingAttempts).isInstanceOf(AsyncData.Uninitialized::class.java)
            }
            awaitItem().also { state ->
                assertThat(state.pinEntry).isInstanceOf(AsyncData.Success::class.java)
                assertThat(state.remainingAttempts).isInstanceOf(AsyncData.Success::class.java)
                state.eventSink(PinUnlockEvent.OnPinKeypadPressed(PinKeypadModel.Number('1')))
                state.eventSink(PinUnlockEvent.OnPinKeypadPressed(PinKeypadModel.Number('2')))
            }
            skipItems(1)
            awaitItem().also { state ->
                state.pinEntry.assertText(halfCompletePin)
                state.eventSink(PinUnlockEvent.OnPinKeypadPressed(PinKeypadModel.Number('3')))
                state.eventSink(PinUnlockEvent.OnPinKeypadPressed(PinKeypadModel.Back))
                state.eventSink(PinUnlockEvent.OnPinKeypadPressed(PinKeypadModel.Empty))
                state.eventSink(PinUnlockEvent.OnPinKeypadPressed(PinKeypadModel.Number('3')))
                state.eventSink(PinUnlockEvent.OnPinKeypadPressed(PinKeypadModel.Number('5')))
            }
            skipItems(4)
            awaitItem().also { state ->
                state.pinEntry.assertText(completePin)
                assertThat(state.isUnlocked).isTrue()
            }
        }
    }

    @Test
    fun `present - failure verify flow`() = runTest {
        val presenter = createPinUnlockPresenter()
        presenter.test {
            skipItems(1)
            val initialState = awaitItem().also { state ->
                assertThat(state.pinEntry).isInstanceOf(AsyncData.Success::class.java)
                assertThat(state.remainingAttempts).isInstanceOf(AsyncData.Success::class.java)
            }
            val numberOfAttempts = initialState.remainingAttempts.dataOrNull() ?: 0
            repeat(numberOfAttempts) {
                initialState.eventSink(PinUnlockEvent.OnPinKeypadPressed(PinKeypadModel.Number('1')))
                initialState.eventSink(PinUnlockEvent.OnPinKeypadPressed(PinKeypadModel.Number('2')))
                initialState.eventSink(PinUnlockEvent.OnPinKeypadPressed(PinKeypadModel.Number('3')))
                initialState.eventSink(PinUnlockEvent.OnPinKeypadPressed(PinKeypadModel.Number('4')))
            }
            skipItems(4 * numberOfAttempts + 2)
            awaitItem().also { state ->
                assertThat(state.remainingAttempts.dataOrNull()).isEqualTo(0)
                assertThat(state.showSignOutPrompt).isTrue()
                assertThat(state.isSignOutPromptCancellable).isFalse()
            }
        }
    }

    @Test
    fun `present - forgot pin flow`() = runTest {
        val signOutLambda = lambdaRecorder<Boolean, Unit> {}
        val signOut = FakeLogoutUseCase(signOutLambda)
        val presenter = createPinUnlockPresenter(logoutUseCase = signOut)
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.pinEntry).isInstanceOf(AsyncData.Success::class.java)
                assertThat(state.remainingAttempts).isInstanceOf(AsyncData.Success::class.java)
                state.eventSink(PinUnlockEvent.OnForgetPin)
            }
            awaitItem().also { state ->
                assertThat(state.showSignOutPrompt).isTrue()
                assertThat(state.isSignOutPromptCancellable).isTrue()
                state.eventSink(PinUnlockEvent.ClearSignOutPrompt)
            }
            awaitItem().also { state ->
                assertThat(state.showSignOutPrompt).isFalse()
                state.eventSink(PinUnlockEvent.OnForgetPin)
            }
            awaitItem().also { state ->
                assertThat(state.showSignOutPrompt).isTrue()
                state.eventSink(PinUnlockEvent.SignOut)
            }
            skipItems(2)
            awaitItem().also { state ->
                assertThat(state.signOutAction).isInstanceOf(AsyncAction.Success::class.java)
            }
            assert(signOutLambda).isCalledOnce()
        }
    }

    @Test
    fun `present - pin is configured, but deleted in store, sign out prompt will be shown`() = runTest {
        val lockScreenStore = InMemoryLockScreenStore()
        val pinCodeManager = aPinCodeManager(
            lockScreenStore = lockScreenStore,
        )
        val presenter = createPinUnlockPresenter(
            pinCodeManager = pinCodeManager,
        )
        // Delete the pin code from the store
        lockScreenStore.deleteEncryptedPinCode()
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.pinEntry).isInstanceOf(AsyncData.Failure::class.java)
                assertThat(state.showSignOutPrompt).isTrue()
                assertThat(state.isSignOutPromptCancellable).isFalse()
                assertThat(state.remainingAttempts.dataOrNull()).isEqualTo(3)
            }
        }
    }

    @Test
    fun `present - forDeviceUnlock is exposed in state`() = runTest {
        val presenter = createPinUnlockPresenter(forDeviceUnlock = true)
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.canNavigateBack).isTrue()
            }
        }
    }

    @Test
    fun `present - pin entry changed event updates pin entry`() = runTest {
        val presenter = createPinUnlockPresenter()
        presenter.test {
            skipItems(1)
            awaitItem().also { state ->
                state.eventSink(PinUnlockEvent.OnPinEntryChanged(halfCompletePin))
            }
            awaitItem().also { state ->
                state.pinEntry.assertText(halfCompletePin)
            }
        }
    }

    private fun AsyncData<PinEntry>.assertText(text: String) {
        dataOrNull()?.assertText(text)
    }

    private suspend fun TestScope.createPinUnlockPresenter(
        forDeviceUnlock: Boolean = false,
        biometricAuthenticatorManager: BiometricAuthenticatorManager = FakeBiometricAuthenticatorManager(),
        callback: PinCodeManager.Callback = DefaultPinCodeManagerCallback(),
        logoutUseCase: FakeLogoutUseCase = FakeLogoutUseCase(logoutLambda = {}),
        pinCodeManager: PinCodeManager = aPinCodeManager()
    ): PinUnlockPresenter {
        pinCodeManager.apply {
            addCallback(callback)
            createPinCode(completePin)
        }
        return PinUnlockPresenter(
            forDeviceUnlock = forDeviceUnlock,
            pinCodeManager = pinCodeManager,
            biometricAuthenticatorManager = biometricAuthenticatorManager,
            logoutUseCase = logoutUseCase,
            coroutineScope = this,
            pinUnlockHelper = PinUnlockHelper(biometricAuthenticatorManager, pinCodeManager),
        )
    }
}
