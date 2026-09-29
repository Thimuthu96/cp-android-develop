/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.appnav

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.appnav.root.RootPresenter
import cloud.orelit.communicationplatform.features.rageshake.api.crash.aCrashDetectionState
import cloud.orelit.communicationplatform.features.rageshake.api.detection.aRageshakeDetectionState
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeSdkMetadata
import cloud.orelit.communicationplatform.services.analytics.test.FakeAnalyticsService
import cloud.orelit.communicationplatform.services.apperror.api.AppErrorState
import cloud.orelit.communicationplatform.services.apperror.api.AppErrorStateService
import cloud.orelit.communicationplatform.services.apperror.test.FakeAppErrorStateService
import cloud.orelit.communicationplatform.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class RootPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createRootPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.crashDetectionState.crashDetected).isFalse()
        }
    }

    @Test
    fun `present - passes app error state`() = runTest {
        val presenter = createRootPresenter(
            appErrorService = FakeAppErrorStateService().apply {
                setAppErrorState(
                    AppErrorState.Error(
                        title = "Bad news",
                        body = "Something bad happened",
                        dismiss = {
                            setAppErrorState(AppErrorState.NoError)
                        }
                    )
                )
            }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.errorState).isInstanceOf(AppErrorState.Error::class.java)
            val initialErrorState = initialState.errorState as AppErrorState.Error
            assertThat(initialErrorState.title).isEqualTo("Bad news")
            assertThat(initialErrorState.body).isEqualTo("Something bad happened")

            initialErrorState.dismiss()
            assertThat(awaitItem().errorState).isInstanceOf(AppErrorState.NoError::class.java)
        }
    }

    private fun createRootPresenter(
        appErrorService: AppErrorStateService = FakeAppErrorStateService(),
    ): RootPresenter {
        return RootPresenter(
            crashDetectionPresenter = { aCrashDetectionState() },
            rageshakeDetectionPresenter = { aRageshakeDetectionState() },
            appErrorStateService = appErrorService,
            analyticsService = FakeAnalyticsService(),
            sdkMetadata = FakeSdkMetadata("sha")
        )
    }
}
