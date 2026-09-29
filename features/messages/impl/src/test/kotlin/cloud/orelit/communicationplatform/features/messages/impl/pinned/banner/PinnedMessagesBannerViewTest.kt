/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.messages.impl.pinned.banner

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalledWithParam
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnce
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnceWithParam
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class PinnedMessagesBannerViewTest : RobolectricTest() {
    @Test
    fun `clicking on the banner invoke expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PinnedMessagesBannerEvent>()
        val state = aLoadedPinnedMessagesBannerState(
            eventSink = eventsRecorder
        )
        val pinnedEventId = state.currentPinnedMessage.eventId
        ensureCalledOnceWithParam(pinnedEventId) { callback ->
            setPinnedMessagesBannerView(
                state = state,
                onClick = callback
            )
            onRoot().performClick()
            eventsRecorder.assertSingle(PinnedMessagesBannerEvent.MoveToNextPinned)
        }
    }

    @Test
    fun `clicking on view all emit the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<PinnedMessagesBannerEvent>(expectEvents = true)
        val state = aLoadedPinnedMessagesBannerState(
            eventSink = eventsRecorder
        )
        ensureCalledOnce { callback ->
            setPinnedMessagesBannerView(
                state = state,
                onViewAllClick = callback
            )
            clickOn(CommonStrings.screen_room_pinned_banner_view_all_button_title)
        }
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setPinnedMessagesBannerView(
    state: PinnedMessagesBannerState,
    onClick: (EventId) -> Unit = EnsureNeverCalledWithParam(),
    onViewAllClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        PinnedMessagesBannerView(
            state = state,
            onClick = onClick,
            onViewAllClick = onViewAllClick
        )
    }
}
