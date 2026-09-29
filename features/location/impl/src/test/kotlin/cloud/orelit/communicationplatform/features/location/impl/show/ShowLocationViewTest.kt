/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.location.impl.show

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.features.location.api.Location
import cloud.orelit.communicationplatform.features.location.impl.common.ui.LocationConstraintsDialogState
import cloud.orelit.communicationplatform.libraries.testtags.TestTags
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnce
import cloud.orelit.communicationplatform.tests.testutils.pressBack
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class ShowLocationViewTest : RobolectricTest() {
    @Test
    fun `test back action`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ShowLocationEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setShowLocationView(
                state = aShowLocationState(
                    eventSink = eventsRecorder
                ),
                onBackClick = callback,
            )
            pressBack()
        }
    }

    @Test
    fun `test share action`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ShowLocationEvent>()
        setShowLocationView(
            aShowLocationState(
                eventSink = eventsRecorder
            ),
            onBackClick = EnsureNeverCalled(),
        )
        val shareContentDescription = activity!!.getString(CommonStrings.action_share)
        onNodeWithContentDescription(shareContentDescription).performClick()
        // The default aStaticLocationMode uses Location(1.23, 2.34, 4f)
        eventsRecorder.assertSingle(ShowLocationEvent.Share(Location(1.23, 2.34, 4f)))
    }

    @Test
    fun `test fab click`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ShowLocationEvent>()
        setShowLocationView(
            aShowLocationState(
                eventSink = eventsRecorder
            ),
            onBackClick = EnsureNeverCalled(),
        )
        onNodeWithTag(TestTags.floatingActionButton.value).performClick()
        eventsRecorder.assertSingle(ShowLocationEvent.TrackMyLocation(true))
    }

    @Test
    fun `when permission denied is displayed user can open the settings`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ShowLocationEvent>()
        setShowLocationView(
            aShowLocationState(
                constraintsDialogState = LocationConstraintsDialogState.PermissionDenied,
                eventSink = eventsRecorder
            ),
            onBackClick = EnsureNeverCalled(),
        )
        clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(ShowLocationEvent.OpenAppSettings)
    }

    @Test
    fun `when permission denied is displayed user can close the dialog`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ShowLocationEvent>()
        setShowLocationView(
            aShowLocationState(
                constraintsDialogState = LocationConstraintsDialogState.PermissionDenied,
                eventSink = eventsRecorder
            ),
            onBackClick = EnsureNeverCalled(),
        )
        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ShowLocationEvent.DismissDialog)
    }

    @Test
    fun `when permission rationale is displayed user can request permissions`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ShowLocationEvent>()
        setShowLocationView(
            aShowLocationState(
                constraintsDialogState = LocationConstraintsDialogState.PermissionRationale,
                eventSink = eventsRecorder
            ),
            onBackClick = EnsureNeverCalled(),
        )
        clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(ShowLocationEvent.RequestPermissions)
    }

    @Test
    fun `when permission rationale is displayed user can close the dialog`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ShowLocationEvent>()
        setShowLocationView(
            aShowLocationState(
                constraintsDialogState = LocationConstraintsDialogState.PermissionRationale,
                eventSink = eventsRecorder
            ),
            onBackClick = EnsureNeverCalled(),
        )
        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(ShowLocationEvent.DismissDialog)
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setShowLocationView(
    state: ShowLocationState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        // Simulate a LocalInspectionMode for MapLibreMap
        CompositionLocalProvider(LocalInspectionMode provides true) {
            ShowLocationView(
                state = state,
                onBackClick = onBackClick,
            )
        }
    }
}
