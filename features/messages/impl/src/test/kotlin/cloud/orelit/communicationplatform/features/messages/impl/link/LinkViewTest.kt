/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.messages.impl.link

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalledWithParam
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnceWithParam
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import io.element.android.wysiwyg.link.Link
import org.junit.Test

class LinkViewTest : RobolectricTest() {
    @Test
    fun `clicking on cancel emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LinkEvent>()
        setLinkView(
            aLinkState(
                linkClick = ConfirmingLinkClick(aLink),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(
            LinkEvent.Cancel
        )
    }

    @Test
    fun `clicking on continue emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LinkEvent>()
        setLinkView(
            aLinkState(
                linkClick = ConfirmingLinkClick(aLink),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_continue)
        eventsRecorder.assertSingle(
            LinkEvent.Confirm
        )
    }

    @Test
    fun `success state invokes the callback and emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<LinkEvent>()
        ensureCalledOnceWithParam(aLink) { callback ->
            setLinkView(
                aLinkState(
                    linkClick = AsyncAction.Success(aLink),
                    eventSink = eventsRecorder,
                ),
                onLinkValid = callback,
            )
        }
        eventsRecorder.assertSingle(
            LinkEvent.Cancel
        )
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setLinkView(
    state: LinkState,
    onLinkValid: (Link) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        LinkView(
            state = state,
            onLinkValid = onLinkValid,
        )
    }
}
