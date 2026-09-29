/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.matrix.ui.messages.reply.InReplyToDetails
import cloud.orelit.communicationplatform.libraries.matrix.ui.messages.reply.InReplyToDetailsInformativePreviewParam

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithReplyInformativePreview(
    @PreviewParameter(InReplyToDetailsInformativePreviewParam::class) inReplyToDetails: InReplyToDetails,
) = ElementPreview {
    TimelineItemEventRowWithReplyContentToPreview(inReplyToDetails)
}
