/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.lockscreen.impl.setup.pin

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.lockscreen.impl.LockScreenConfig
import cloud.orelit.communicationplatform.features.lockscreen.impl.fixtures.aLockScreenConfig
import cloud.orelit.communicationplatform.features.lockscreen.impl.fixtures.aPinCodeManager
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.DefaultPinCodeManagerCallback
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.PinCodeManager
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.model.assertEmpty
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.model.assertText
import cloud.orelit.communicationplatform.features.lockscreen.impl.setup.pin.validation.PinValidator
import cloud.orelit.communicationplatform.features.lockscreen.impl.setup.pin.validation.SetupPinFailure
import cloud.orelit.communicationplatform.libraries.matrix.test.core.aBuildMeta
import cloud.orelit.communicationplatform.tests.testutils.awaitLastSequentialItem
import cloud.orelit.communicationplatform.tests.testutils.consumeItemsUntilPredicate
import cloud.orelit.communicationplatform.tests.testutils.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SetupPinPresenterTest {
    private val forbiddenPin = "1234"
    private val halfCompletePin = "12"
    private val completePin = "1235"
    private val mismatchedPin = "1236"

    @Test
    fun `present - complete flow`() = runTest {
        val pinCodeCreated = CompletableDeferred<Unit>()
        val callback = object : DefaultPinCodeManagerCallback() {
            override fun onPinCodeCreated() {
                pinCodeCreated.complete(Unit)
            }
        }
        val presenter = createSetupPinPresenter(callback)
        presenter.test {
            awaitItem().also { state ->
                state.choosePinEntry.assertEmpty()
                state.confirmPinEntry.assertEmpty()
                assertThat(state.setupPinFailure).isNull()
                assertThat(state.isConfirmationStep).isFalse()
                state.onPinEntryChanged(halfCompletePin)
            }
            awaitItem().also { state ->
                state.choosePinEntry.assertText(halfCompletePin)
                state.confirmPinEntry.assertEmpty()
                assertThat(state.setupPinFailure).isNull()
                assertThat(state.isConfirmationStep).isFalse()
                state.onPinEntryChanged(forbiddenPin)
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertText(forbiddenPin)
                assertThat(state.setupPinFailure).isEqualTo(SetupPinFailure.ForbiddenPin)
                state.eventSink(SetupPinEvent.ClearFailure)
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertEmpty()
                assertThat(state.setupPinFailure).isNull()
                state.onPinEntryChanged(completePin)
            }
            consumeItemsUntilPredicate {
                it.isConfirmationStep
            }.last().also { state ->
                state.choosePinEntry.assertText(completePin)
                state.confirmPinEntry.assertEmpty()
                assertThat(state.isConfirmationStep).isTrue()
                state.onPinEntryChanged(mismatchedPin)
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertText(completePin)
                state.confirmPinEntry.assertText(mismatchedPin)
                assertThat(state.setupPinFailure).isEqualTo(SetupPinFailure.PinsDoNotMatch)
                state.eventSink(SetupPinEvent.ClearFailure)
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertEmpty()
                state.confirmPinEntry.assertEmpty()
                assertThat(state.isConfirmationStep).isFalse()
                assertThat(state.setupPinFailure).isNull()
                state.onPinEntryChanged(completePin)
            }
            consumeItemsUntilPredicate {
                it.isConfirmationStep
            }.last().also { state ->
                state.choosePinEntry.assertText(completePin)
                state.confirmPinEntry.assertEmpty()
                assertThat(state.isConfirmationStep).isTrue()
                state.onPinEntryChanged(completePin)
            }
            awaitItem().also { state ->
                state.choosePinEntry.assertText(completePin)
                state.confirmPinEntry.assertText(completePin)
            }
            pinCodeCreated.await()
        }
    }

    private fun SetupPinState.onPinEntryChanged(pinEntry: String) {
        eventSink(SetupPinEvent.OnPinEntryChanged(pinEntry, isConfirmationStep))
    }

    private fun createSetupPinPresenter(
        callback: PinCodeManager.Callback,
        lockScreenConfig: LockScreenConfig = aLockScreenConfig(
            forbiddenPinCodes = setOf(forbiddenPin)
        ),
    ): SetupPinPresenter {
        val pinCodeManager = aPinCodeManager()
        pinCodeManager.addCallback(callback)
        return SetupPinPresenter(
            lockScreenConfig = lockScreenConfig,
            pinValidator = PinValidator(lockScreenConfig),
            buildMeta = aBuildMeta(),
            pinCodeManager = pinCodeManager
        )
    }
}
