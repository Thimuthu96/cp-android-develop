/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.analytics.impl.preferences

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.appconfig.AnalyticsConfig
import cloud.orelit.communicationplatform.features.analytics.api.AnalyticsOptInEvent
import cloud.orelit.communicationplatform.libraries.matrix.test.core.aBuildMeta
import cloud.orelit.communicationplatform.services.analytics.test.FakeAnalyticsService
import cloud.orelit.communicationplatform.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AnalyticsPreferencesPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state available`() = runTest {
        val presenter = AnalyticsPreferencesPresenter(
            FakeAnalyticsService(isEnabled = true, didAskUserConsent = true),
            aBuildMeta()
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isEnabled).isTrue()
            assertThat(initialState.policyUrl).isEqualTo(AnalyticsConfig.POLICY_LINK)
        }
    }

    @Test
    fun `present - initial state not available`() = runTest {
        val presenter = AnalyticsPreferencesPresenter(
            FakeAnalyticsService(isEnabled = false, didAskUserConsent = false),
            aBuildMeta()
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isEnabled).isFalse()
        }
    }

    @Test
    fun `present - enable and disable`() = runTest {
        val presenter = AnalyticsPreferencesPresenter(
            FakeAnalyticsService(isEnabled = true, didAskUserConsent = true),
            aBuildMeta()
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.isEnabled).isTrue()
            initialState.eventSink.invoke(AnalyticsOptInEvent.EnableAnalytics(false))
            assertThat(awaitItem().isEnabled).isFalse()
            initialState.eventSink.invoke(AnalyticsOptInEvent.EnableAnalytics(true))
            assertThat(awaitItem().isEnabled).isTrue()
        }
    }
}
