/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.preferences.impl.blockedusers

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.features.preferences.impl.R
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.aMatrixUserList
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnce
import cloud.orelit.communicationplatform.tests.testutils.pressBack
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class BlockedUserViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invokes back callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<BlockedUsersEvent>(expectEvents = false)
        ensureCalledOnce { callback ->
            setBlockedUsersView(
                aBlockedUsersState(
                    eventSink = eventsRecorder
                ),
                onBackClick = callback,
            )
            pressBack()
        }
    }

    @Test
    fun `clicking on a user emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<BlockedUsersEvent>()
        val userList = aMatrixUserList()
        setBlockedUsersView(
            aBlockedUsersState(
                blockedUsers = userList,
                eventSink = eventsRecorder
            ),
        )
        onNodeWithText(userList.first().displayName.orEmpty()).performClick()
        eventsRecorder.assertSingle(BlockedUsersEvent.Unblock(userList.first().userId))
    }

    @Test
    fun `long clicking on a user emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<BlockedUsersEvent>()
        val userList = aMatrixUserList()
        setBlockedUsersView(
            aBlockedUsersState(
                blockedUsers = userList,
                eventSink = eventsRecorder
            ),
        )
        onNodeWithText(userList.first().displayName.orEmpty()).performTouchInput { longClick() }
        eventsRecorder.assertSingle(BlockedUsersEvent.CopyToClipboard(userList.first().userId))
    }

    @Test
    fun `clicking on cancel sends a BlockedUsersEvent`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<BlockedUsersEvent>()
        setBlockedUsersView(
            aBlockedUsersState(
                unblockUserAction = AsyncAction.ConfirmingNoParams,
                eventSink = eventsRecorder
            ),
        )
        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(BlockedUsersEvent.Cancel)
    }

    @Test
    fun `clicking on confirm sends a BlockedUsersEvent`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<BlockedUsersEvent>()
        setBlockedUsersView(
            aBlockedUsersState(
                unblockUserAction = AsyncAction.ConfirmingNoParams,
                eventSink = eventsRecorder
            ),
        )
        clickOn(R.string.screen_blocked_users_unblock_alert_action)
        eventsRecorder.assertSingle(BlockedUsersEvent.ConfirmUnblock)
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setBlockedUsersView(
    state: BlockedUsersState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        BlockedUsersView(
            state = state,
            onBackClick = onBackClick,
        )
    }
}
