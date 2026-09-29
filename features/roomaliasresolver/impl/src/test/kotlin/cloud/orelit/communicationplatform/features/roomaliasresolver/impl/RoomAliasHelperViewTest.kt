/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.roomaliasresolver.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.matrix.api.room.alias.ResolvedRoomAlias
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalledWithParam
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnce
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnceWithParam
import cloud.orelit.communicationplatform.tests.testutils.pressBack
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class RoomAliasHelperViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomAliasResolverEvent>(expectEvents = false)
        ensureCalledOnce {
            setRoomAliasResolverView(
                aRoomAliasResolverState(
                    eventSink = eventsRecorder,
                ),
                onBackClick = it
            )
            pressBack()
        }
    }

    @Test
    fun `clicking on Retry emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<RoomAliasResolverEvent>()
        setRoomAliasResolverView(
            aRoomAliasResolverState(
                resolveState = AsyncData.Failure(Exception("Error")),
                eventSink = eventsRecorder,
            ),
        )
        clickOn(CommonStrings.action_retry)
        eventsRecorder.assertSingle(RoomAliasResolverEvent.Retry)
    }

    @Test
    fun `success state invokes the expected Callback`() = runAndroidComposeUiTest {
        val result = aResolvedRoomAlias()
        val eventsRecorder = EventsRecorder<RoomAliasResolverEvent>(expectEvents = false)
        ensureCalledOnceWithParam(result) {
            setRoomAliasResolverView(
                aRoomAliasResolverState(
                    resolveState = AsyncData.Success(result),
                    eventSink = eventsRecorder,
                ),
                onAliasResolved = it,
            )
        }
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setRoomAliasResolverView(
    state: RoomAliasResolverState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onAliasResolved: (ResolvedRoomAlias) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        RoomAliasResolverView(
            state = state,
            onBackClick = onBackClick,
            onSuccess = onAliasResolved,
        )
    }
}
