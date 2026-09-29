/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.appnav.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import cloud.orelit.communicationplatform.features.rageshake.api.crash.CrashDetectionEvent
import cloud.orelit.communicationplatform.features.rageshake.api.crash.CrashDetectionView
import cloud.orelit.communicationplatform.features.rageshake.api.detection.RageshakeDetectionEvent
import cloud.orelit.communicationplatform.features.rageshake.api.detection.RageshakeDetectionView
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.services.apperror.api.AppErrorView

@Composable
fun RootView(
    state: RootState,
    onOpenBugReport: () -> Unit,
    modifier: Modifier = Modifier,
    children: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        children()

        fun onOpenBugReport() {
            state.crashDetectionState.eventSink(CrashDetectionEvent.ResetAppHasCrashed)
            state.rageshakeDetectionState.eventSink(RageshakeDetectionEvent.Dismiss)
            onOpenBugReport.invoke()
        }

        RageshakeDetectionView(
            state = state.rageshakeDetectionState,
            onOpenBugReport = ::onOpenBugReport,
        )
        CrashDetectionView(
            state = state.crashDetectionState,
            onOpenBugReport = ::onOpenBugReport,
        )
        AppErrorView(
            state = state.errorState,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun RootViewPreview(@PreviewParameter(RootStatePreviewParam::class) rootState: RootState) = ElementPreview {
    RootView(
        state = rootState,
        onOpenBugReport = {},
    ) {
        Text("Children")
    }
}
