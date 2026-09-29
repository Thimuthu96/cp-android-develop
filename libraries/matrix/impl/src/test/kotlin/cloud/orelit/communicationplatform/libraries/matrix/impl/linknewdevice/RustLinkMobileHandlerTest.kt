/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package cloud.orelit.communicationplatform.libraries.matrix.impl.linknewdevice

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.matrix.api.linknewdevice.ErrorType
import cloud.orelit.communicationplatform.libraries.matrix.api.linknewdevice.LinkMobileStep
import cloud.orelit.communicationplatform.libraries.matrix.impl.fixtures.fakes.FakeFfiCheckCodeSender
import cloud.orelit.communicationplatform.libraries.matrix.impl.fixtures.fakes.FakeFfiContinuationMessageSender
import cloud.orelit.communicationplatform.libraries.matrix.impl.fixtures.fakes.FakeFfiGrantLoginWithQrCodeHandler
import cloud.orelit.communicationplatform.libraries.matrix.impl.fixtures.fakes.FakeFfiQrCodeData
import cloud.orelit.communicationplatform.libraries.matrix.test.QR_CODE_DATA_RECIPROCATE
import cloud.orelit.communicationplatform.tests.testutils.ExpectedResult
import cloud.orelit.communicationplatform.tests.testutils.match
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.GrantGeneratedQrLoginProgress
import org.matrix.rustcomponents.sdk.HumanQrGrantLoginException

class RustLinkMobileHandlerTest {
    @Test
    fun `start function works as expected`() = runTest {
        val completable = CompletableDeferred<Unit>()
        val handler = FakeFfiGrantLoginWithQrCodeHandler(
            generateResult = {
                // Ensure that the coroutine is hold
                completable.await()
            }
        )
        val sut = createRustLinkMobileHandler(
            handler,
        )
        sut.linkMobileStep.test {
            val initialItem = awaitItem()
            assertThat(initialItem).isEqualTo(LinkMobileStep.Uninitialized)
            backgroundScope.launch {
                sut.start()
            }
            runCurrent()
            // progress from the handler is mapped and emitted
            listOf(
                GrantGeneratedQrLoginProgress.Starting to ExpectedResult(LinkMobileStep.Starting),
                GrantGeneratedQrLoginProgress.SyncingSecrets to ExpectedResult(LinkMobileStep.SyncingSecrets),
                GrantGeneratedQrLoginProgress.WaitingForAuth("aVerificationUri", FakeFfiContinuationMessageSender())
                    to ExpectedResult(LinkMobileStep.WaitingForAuth::class.java),
                GrantGeneratedQrLoginProgress.QrScanned(FakeFfiCheckCodeSender())
                    to ExpectedResult(LinkMobileStep.QrScanned::class.java),
                GrantGeneratedQrLoginProgress.QrReady(FakeFfiQrCodeData(toBytesResult = { QR_CODE_DATA_RECIPROCATE }))
                    to ExpectedResult(LinkMobileStep.QrReady::class.java),
                GrantGeneratedQrLoginProgress.Done to ExpectedResult(LinkMobileStep.Done),
            ).forEach { (progress, expectedResult) ->
                handler.emitGenerateProgress(progress)
                assertThat(awaitItem()).match(expectedResult)
            }
            // generate returns, no new event is emitted
            completable.complete(Unit)
            expectNoEvents()
        }
    }

    @Test
    fun `when generates does not emits the Done state, the code emits it`() = runTest {
        val completable = CompletableDeferred<Unit>()
        val handler = FakeFfiGrantLoginWithQrCodeHandler(
            generateResult = {
                // Ensure that the coroutine is hold
                completable.await()
            }
        )
        val sut = createRustLinkMobileHandler(
            handler,
        )
        sut.linkMobileStep.test {
            val initialItem = awaitItem()
            assertThat(initialItem).isEqualTo(LinkMobileStep.Uninitialized)
            backgroundScope.launch {
                sut.start()
            }
            runCurrent()
            handler.emitGenerateProgress(GrantGeneratedQrLoginProgress.Starting)
            assertThat(awaitItem()).isEqualTo(LinkMobileStep.Starting)
            // generate returns, Done event is emitted
            completable.complete(Unit)
            assertThat(awaitItem()).isEqualTo(LinkMobileStep.Done)
        }
    }

    @Test
    fun `when start throws HumanQrGrantLoginException, the handler emits error step`() = runTest {
        val handler = FakeFfiGrantLoginWithQrCodeHandler(
            generateResult = { throw HumanQrGrantLoginException.NotFound("Timeout") }
        )
        val sut = createRustLinkMobileHandler(
            handler,
        )
        sut.linkMobileStep.test {
            val initialItem = awaitItem()
            assertThat(initialItem).isEqualTo(LinkMobileStep.Uninitialized)
            backgroundScope.launch {
                sut.start()
            }
            runCurrent()
            val errorState = awaitItem()
            assertThat(errorState).isInstanceOf(LinkMobileStep.Error::class.java)
            val errorType = (errorState as LinkMobileStep.Error).errorType
            assertThat(errorType).isInstanceOf(ErrorType.NotFound::class.java)
        }
    }

    @Test
    fun `when start throws HumanQrGrantLoginException_NotFound when in state QrReady, the handler emits QrRotating step`() = runTest {
        val completable = CompletableDeferred<Unit>()
        val handler = FakeFfiGrantLoginWithQrCodeHandler(
            generateResult = {
                completable.await()
                throw HumanQrGrantLoginException.NotFound("Timeout")
            }
        )
        val sut = createRustLinkMobileHandler(
            handler,
        )
        sut.linkMobileStep.test {
            val initialItem = awaitItem()
            assertThat(initialItem).isEqualTo(LinkMobileStep.Uninitialized)
            backgroundScope.launch {
                sut.start()
            }
            runCurrent()
            handler.emitGenerateProgress(GrantGeneratedQrLoginProgress.QrReady(FakeFfiQrCodeData(toBytesResult = { QR_CODE_DATA_RECIPROCATE })))
            val readyState = awaitItem()
            assertThat(readyState).isInstanceOf(LinkMobileStep.QrReady::class.java)
            // generate returns, error is emitted
            completable.complete(Unit)
            val qrRotatingState = awaitItem()
            assertThat(qrRotatingState).isEqualTo(LinkMobileStep.QrRotating)
        }
    }

    @Test
    fun `when start throws HumanQrGrantLoginException_Expired, the handler emits QrRotating step`() = runTest {
        val completable = CompletableDeferred<Unit>()
        val handler = FakeFfiGrantLoginWithQrCodeHandler(
            generateResult = {
                completable.await()
                throw HumanQrGrantLoginException.Expired("Expired")
            }
        )
        val sut = createRustLinkMobileHandler(
            handler,
        )
        sut.linkMobileStep.test {
            val initialItem = awaitItem()
            assertThat(initialItem).isEqualTo(LinkMobileStep.Uninitialized)
            backgroundScope.launch {
                sut.start()
            }
            runCurrent()
            // generate returns, error is emitted
            completable.complete(Unit)
            val qrRotatingState = awaitItem()
            assertThat(qrRotatingState).isEqualTo(LinkMobileStep.QrRotating)
        }
    }

    private fun TestScope.createRustLinkMobileHandler(
        handler: FakeFfiGrantLoginWithQrCodeHandler = FakeFfiGrantLoginWithQrCodeHandler(),
    ) = RustLinkMobileHandler(
        inner = handler,
        sessionCoroutineScope = backgroundScope,
        sessionDispatcher = StandardTestDispatcher(testScheduler),
    )
}
