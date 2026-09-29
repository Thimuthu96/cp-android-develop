/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.services.analytics.impl

import androidx.lifecycle.Lifecycle
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.MobileScreen
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService
import cloud.orelit.communicationplatform.services.analytics.test.FakeAnalyticsService
import cloud.orelit.communicationplatform.services.toolbox.api.systemclock.SystemClock
import cloud.orelit.communicationplatform.services.toolbox.test.systemclock.FakeSystemClock
import cloud.orelit.communicationplatform.tests.testutils.FakeLifecycleOwner
import cloud.orelit.communicationplatform.tests.testutils.withFakeLifecycleOwner
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultScreenTrackerTest {
    @Test
    fun `TrackScreen is working as expected`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val systemClock = FakeSystemClock(150)
        val lifecycleOwner = FakeLifecycleOwner()
        val sut = createDefaultScreenTracker(
            analyticsService = analyticsService,
            systemClock = systemClock,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            withFakeLifecycleOwner(lifecycleOwner) {
                sut.TrackScreen(MobileScreen.ScreenName.RoomMembers)
            }
        }.test {
            // Screen resumes
            lifecycleOwner.givenState(Lifecycle.State.RESUMED)
            assertThat(awaitItem()).isEqualTo(Unit)
            systemClock.epochMillisResult = 450
            lifecycleOwner.givenState(Lifecycle.State.DESTROYED)
        }
        assertThat(analyticsService.screenEvents).containsExactly(
            MobileScreen(
                screenName = MobileScreen.ScreenName.RoomMembers,
                durationMs = 300,
            )
        )
    }
}

private fun createDefaultScreenTracker(
    analyticsService: AnalyticsService = FakeAnalyticsService(),
    systemClock: SystemClock = FakeSystemClock(),
) = DefaultScreenTracker(
    analyticsService = analyticsService,
    systemClock = systemClock,
)
