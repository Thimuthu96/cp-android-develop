/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.fileviewer

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.local.LocalMedia
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnce
import cloud.orelit.communicationplatform.tests.testutils.pressBack
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class FileViewerViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invokes the expected callback`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<FileViewerEvent>(expectEvents = false)
        ensureCalledOnce {
            setFileViewerView(
                state = aFileViewerState(eventSink = eventsRecorder),
                onBackClick = it,
            )
            pressBack()
        }
    }

    @Test
    fun `clicking on share emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<FileViewerEvent>()
        setFileViewerView(
            state = aFileViewerState(
                localMedia = AsyncData.Success(LocalMedia(Uri.EMPTY, aJsonMediaInfo())),
                eventSink = eventsRecorder,
            ),
        )
        clickOnContentDescription(CommonStrings.action_share)
        eventsRecorder.assertSingle(FileViewerEvent.Share)
    }

    @Test
    fun `clicking on download emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<FileViewerEvent>()
        setFileViewerView(
            state = aFileViewerState(
                localMedia = AsyncData.Success(LocalMedia(Uri.EMPTY, aJsonMediaInfo())),
                eventSink = eventsRecorder,
            ),
        )
        clickOnContentDescription(CommonStrings.action_download)
        eventsRecorder.assertSingle(FileViewerEvent.SaveOnDisk)
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.clickOnContentDescription(@StringRes res: Int) {
    val text = activity!!.getString(res)
    onNode(hasContentDescription(text) and hasClickAction()).performClick()
}

private fun AndroidComposeUiTest<ComponentActivity>.setFileViewerView(
    state: FileViewerState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        FileViewerView(
            state = state,
            textFileViewer = { _, _ -> },
            onBackClick = onBackClick,
        )
    }
}
