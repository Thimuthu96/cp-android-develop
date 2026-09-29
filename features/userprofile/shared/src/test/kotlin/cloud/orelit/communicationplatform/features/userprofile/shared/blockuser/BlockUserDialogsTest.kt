/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.userprofile.shared.blockuser

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.features.userprofile.api.UserProfileEvent
import cloud.orelit.communicationplatform.features.userprofile.api.UserProfileState
import cloud.orelit.communicationplatform.features.userprofile.shared.R
import cloud.orelit.communicationplatform.features.userprofile.shared.aUserProfileState
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class BlockUserDialogsTest : RobolectricTest() {
    @Test
    fun `confirm block user emit expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<UserProfileEvent>()
        setContent {
            BlockUserDialogs(
                state = aUserProfileState(
                    displayConfirmationDialog = UserProfileState.ConfirmationDialog.Block,
                    eventSink = eventsRecorder,
                )
            )
        }
        clickOn(R.string.screen_dm_details_block_alert_action)
        eventsRecorder.assertSingle(UserProfileEvent.BlockUser(false))
    }

    @Test
    fun `cancel block user emit expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<UserProfileEvent>()
        setContent {
            BlockUserDialogs(
                state = aUserProfileState(
                    displayConfirmationDialog = UserProfileState.ConfirmationDialog.Block,
                    eventSink = eventsRecorder,
                )
            )
        }
        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(UserProfileEvent.ClearConfirmationDialog)
    }

    @Test
    fun `confirm unblock user emit expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<UserProfileEvent>()
        setContent {
            BlockUserDialogs(
                state = aUserProfileState(
                    displayConfirmationDialog = UserProfileState.ConfirmationDialog.Unblock,
                    eventSink = eventsRecorder,
                )
            )
        }
        clickOn(R.string.screen_dm_details_unblock_alert_action)
        eventsRecorder.assertSingle(UserProfileEvent.UnblockUser(false))
    }

    @Test
    fun `cancel unblock user emit expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<UserProfileEvent>()
        setContent {
            BlockUserDialogs(
                state = aUserProfileState(
                    displayConfirmationDialog = UserProfileState.ConfirmationDialog.Unblock,
                    eventSink = eventsRecorder,
                )
            )
        }
        clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(UserProfileEvent.ClearConfirmationDialog)
    }
}
