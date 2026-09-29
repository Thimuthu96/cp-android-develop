/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.details

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalledWithParam
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnce
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnceWithParam
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import cloud.orelit.communicationplatform.tests.testutils.setSafeContent
import org.junit.Test

class MediaDeleteConfirmationBottomSheetTest : RobolectricTest() {
    @Test
    fun `clicking on Cancel invokes expected callback`() = runAndroidComposeUiTest {
        val state = aMediaBottomSheetStateDeleteConfirmation()
        ensureCalledOnce { callback ->
            setMediaDeleteConfirmationBottomSheet(
                state = state,
                onDismiss = callback,
            )
            clickOn(CommonStrings.action_cancel)
        }
    }

    @Test
    fun `clicking on Remove invokes expected callback`() = runAndroidComposeUiTest {
        val state = aMediaBottomSheetStateDeleteConfirmation()
        ensureCalledOnceWithParam(state.eventId) { callback ->
            setMediaDeleteConfirmationBottomSheet(
                state = state,
                onDelete = callback,
            )
            onNodeWithText(activity!!.getString(CommonStrings.action_remove)).assertExists()
            clickOn(CommonStrings.action_remove)
        }
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setMediaDeleteConfirmationBottomSheet(
    state: MediaBottomSheetState.DeleteConfirmation,
    onDelete: (EventId) -> Unit = EnsureNeverCalledWithParam(),
    onDismiss: () -> Unit = EnsureNeverCalled(),
) {
    setSafeContent {
        MediaDeleteConfirmationBottomSheet(
            state = state,
            onDelete = onDelete,
            onDismiss = onDismiss,
        )
    }
}
