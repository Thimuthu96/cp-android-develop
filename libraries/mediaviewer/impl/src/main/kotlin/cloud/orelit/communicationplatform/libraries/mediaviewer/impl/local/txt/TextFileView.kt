/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.local.txt

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import cloud.orelit.communicationplatform.features.viewfolder.api.TextFileViewer
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.core.extensions.runCatchingExceptions
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.CircularProgressIndicator
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.local.LocalMedia
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.viewer.topAppBarHeight
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun TextFileView(
    localMedia: LocalMedia?,
    textFileViewer: TextFileViewer,
    modifier: Modifier = Modifier,
) {
    val data = remember { mutableStateOf<AsyncData<ImmutableList<String>>>(AsyncData.Uninitialized) }
    val context = LocalContext.current
    LaunchedEffect(localMedia?.uri) {
        data.value = AsyncData.Loading()
        if (localMedia?.uri != null) {
            // Load the file content
            val result = runCatchingExceptions {
                context.contentResolver.openInputStream(localMedia.uri).use {
                    it?.bufferedReader()?.readLines()?.toList().orEmpty()
                }
            }
            data.value = if (result.isSuccess) {
                AsyncData.Success(result.getOrNull().orEmpty().toImmutableList())
            } else {
                AsyncData.Failure(result.exceptionOrNull() ?: Exception("An error occurred"))
            }
        }
    }
    TextFileContentView(
        data = data.value,
        textFileViewer = textFileViewer,
        modifier = modifier,
    )
}

@Composable
private fun TextFileContentView(
    data: AsyncData<ImmutableList<String>>,
    textFileViewer: TextFileViewer,
    modifier: Modifier = Modifier,
) {
    when (data) {
        AsyncData.Uninitialized,
        is AsyncData.Loading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        is AsyncData.Failure -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = data.error.message ?: stringResource(id = CommonStrings.error_unknown))
        }
        is AsyncData.Success -> {
            textFileViewer.Render(
                lines = data.data,
                modifier = modifier
                    .fillMaxSize()
                    .padding(top = topAppBarHeight),
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TextFileContentViewPreview(
    @PreviewParameter(TextFileContentPreviewParam::class) text: AsyncData<ImmutableList<String>>,
) = ElementPreview {
    TextFileContentView(
        data = text,
        textFileViewer = { lines, modifier ->
            Text(
                modifier = modifier,
                text = lines.firstOrNull() ?: "File content"
            )
        }
    )
}
