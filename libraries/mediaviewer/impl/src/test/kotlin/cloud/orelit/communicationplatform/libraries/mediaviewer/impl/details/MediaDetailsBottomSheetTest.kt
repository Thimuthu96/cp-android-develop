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
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnceWithParam
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import cloud.orelit.communicationplatform.tests.testutils.setSafeContent
import org.junit.Test
import org.robolectric.annotation.Config

class MediaDetailsBottomSheetTest : RobolectricTest() {
    @Test
    @Config(qualifiers = "h1024dp")
    fun `clicking on View in timeline invokes expected callback`() = runAndroidComposeUiTest {
        val state = aMediaBottomSheetStateDetails()
        ensureCalledOnceWithParam(state.eventId) { callback ->
            setMediaDetailsBottomSheet(
                state = state,
                onViewInTimeline = callback,
            )
            clickOn(CommonStrings.action_view_in_timeline)
        }
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `clicking on Share invokes expected callback`() = runAndroidComposeUiTest {
        val state = aMediaBottomSheetStateDetails(
            fromGallery = true,
        )
        ensureCalledOnceWithParam(state.eventId) { callback ->
            setMediaDetailsBottomSheet(
                state = state,
                onShare = callback,
            )
            clickOn(CommonStrings.action_share)
        }
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `item Share is not displayed when opened from the media viewer`() = runAndroidComposeUiTest {
        setMediaDetailsBottomSheet(
            state = aMediaBottomSheetStateDetails(),
        )
        onNodeWithText(activity!!.getString(CommonStrings.action_share)).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `clicking on Forward invokes expected callback`() = runAndroidComposeUiTest {
        val state = aMediaBottomSheetStateDetails()
        ensureCalledOnceWithParam(state.eventId) { callback ->
            setMediaDetailsBottomSheet(
                state = state,
                onForward = callback,
            )
            clickOn(CommonStrings.action_forward)
        }
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `clicking on Download invokes expected callback`() = runAndroidComposeUiTest {
        val state = aMediaBottomSheetStateDetails(
            fromGallery = true,
        )
        ensureCalledOnceWithParam(state.eventId) { callback ->
            setMediaDetailsBottomSheet(
                state = state,
                onDownload = callback,
            )
            clickOn(CommonStrings.action_download)
        }
    }

    @Test
    @Config(qualifiers = "h1024dp")
    fun `item Download is not displayed when opened from the media viewer`() = runAndroidComposeUiTest {
        setMediaDetailsBottomSheet(
            state = aMediaBottomSheetStateDetails(),
        )
        onNodeWithText(activity!!.getString(CommonStrings.action_download)).assertDoesNotExist()
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on Delete invokes expected callback`() = runAndroidComposeUiTest {
        val state = aMediaBottomSheetStateDetails()
        ensureCalledOnceWithParam(state.eventId) { callback ->
            setMediaDetailsBottomSheet(
                state = state,
                onDelete = callback,
            )
            onNodeWithText(activity!!.getString(CommonStrings.action_delete_file)).assertExists()
            clickOn(CommonStrings.action_delete_file)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `Remove is not present if canDelete is false`() = runAndroidComposeUiTest {
        val state = aMediaBottomSheetStateDetails(
            canDelete = false,
        )
        setMediaDetailsBottomSheet(
            state = state,
        )
        onNodeWithText(activity!!.getString(CommonStrings.action_remove)).assertDoesNotExist()
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setMediaDetailsBottomSheet(
    state: MediaBottomSheetState.Details,
    onViewInTimeline: (EventId) -> Unit = EnsureNeverCalledWithParam(),
    onShare: (EventId) -> Unit = EnsureNeverCalledWithParam(),
    onForward: (EventId) -> Unit = EnsureNeverCalledWithParam(),
    onDownload: (EventId) -> Unit = EnsureNeverCalledWithParam(),
    onOpenWith: (EventId) -> Unit = EnsureNeverCalledWithParam(),
    onDelete: (EventId) -> Unit = EnsureNeverCalledWithParam(),
    onDismiss: () -> Unit = EnsureNeverCalled(),
) {
    setSafeContent {
        MediaDetailsBottomSheet(
            state = state,
            onViewInTimeline = onViewInTimeline,
            onShare = onShare,
            onForward = onForward,
            onDownload = onDownload,
            onOpenWith = onOpenWith,
            onDelete = onDelete,
            onDismiss = onDismiss,
        )
    }
}
