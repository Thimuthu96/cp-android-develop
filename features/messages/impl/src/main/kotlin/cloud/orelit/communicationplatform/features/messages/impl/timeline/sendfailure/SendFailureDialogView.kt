/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.sendfailure

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import cloud.orelit.communicationplatform.features.messages.impl.R
import cloud.orelit.communicationplatform.features.messages.impl.timeline.model.TimelineItem
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.ConfirmationDialog
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
fun SendFailureDialogView(
    sendFailureDialogState: SendFailureDialogState,
    onDismiss: () -> Unit,
    onRetry: (TimelineItem.Event) -> Unit,
    onRemoveMessage: (TimelineItem.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (sendFailureDialogState) {
        is SendFailureDialogState.Hidden -> Unit
        is SendFailureDialogState.Show -> {
            // Show the dialog with the message and actions
            val content = when (sendFailureDialogState.sendFailureType) {
                is SendFailureDialogState.SendFailureType.InvalidMimeType -> stringResource(
                    id = R.string.error_sending_failure_invalid_mime_type,
                    sendFailureDialogState.sendFailureType.mimeType,
                )
                is SendFailureDialogState.SendFailureType.MissingMediaContent -> stringResource(
                    id = R.string.error_sending_failure_missing_media_content,
                )
                is SendFailureDialogState.SendFailureType.SendingFromUnverifiedDevice -> stringResource(
                    id = R.string.error_sending_failure_sending_from_unverified_device,
                )
                is SendFailureDialogState.SendFailureType.Error -> stringResource(
                    id = R.string.error_sending_failure_other,
                    sendFailureDialogState.sendFailureType.message
                )
                is SendFailureDialogState.SendFailureType.Unknown -> stringResource(
                    id = R.string.error_sending_failure_unknown,
                )
            }

            ConfirmationDialog(
                modifier = modifier,
                title = stringResource(id = CommonStrings.common_sending_failed),
                content = content,
                onDismiss = onDismiss,
                submitText = stringResource(id = CommonStrings.action_retry),
                onSubmitClick = { onRetry(sendFailureDialogState.event) },
                onCancelClick = onDismiss,
                thirdButtonText = stringResource(id = CommonStrings.action_remove_message),
                onThirdButtonClick = { onRemoveMessage(sendFailureDialogState.event) },
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun SendFailureDialogViewPreview(
    @PreviewParameter(SendFailureDialogStatePreviewParam::class) sendFailureDialogState: SendFailureDialogState,
) = ElementPreview {
    SendFailureDialogView(
        sendFailureDialogState = sendFailureDialogState,
        onDismiss = {},
        onRetry = {},
        onRemoveMessage = {},
    )
}
