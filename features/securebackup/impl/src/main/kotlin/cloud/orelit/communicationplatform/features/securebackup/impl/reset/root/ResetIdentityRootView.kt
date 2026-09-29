/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.securebackup.impl.reset.root

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.securebackup.impl.R
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.pages.FlowStepPage
import cloud.orelit.communicationplatform.libraries.designsystem.components.BigIcon
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.ConfirmationDialog
import cloud.orelit.communicationplatform.libraries.designsystem.components.visuallist.VisualList
import cloud.orelit.communicationplatform.libraries.designsystem.components.visuallist.VisualListItemData
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Button
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import kotlinx.collections.immutable.persistentListOf

@Composable
fun ResetIdentityRootView(
    state: ResetIdentityRootState,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowStepPage(
        modifier = modifier,
        iconStyle = BigIcon.Style.AlertSolid,
        title = stringResource(R.string.screen_encryption_reset_title),
        isScrollable = true,
        content = { Content() },
        buttons = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.screen_encryption_reset_action_continue_reset),
                onClick = { state.eventSink(ResetIdentityRootEvent.Continue) },
                destructive = true,
            )
        },
        onBackClick = onBack,
    )

    if (state.displayConfirmationDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.screen_reset_encryption_confirmation_alert_title),
            content = stringResource(R.string.screen_reset_encryption_confirmation_alert_subtitle),
            submitText = stringResource(R.string.screen_reset_encryption_confirmation_alert_action),
            onSubmitClick = {
                state.eventSink(ResetIdentityRootEvent.DismissDialog)
                onContinue()
            },
            destructiveSubmit = true,
            onDismiss = { state.eventSink(ResetIdentityRootEvent.DismissDialog) }
        )
    }
}

@Composable
private fun Content() {
    Column(
        modifier = Modifier.padding(top = 8.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        VisualList(
            modifier = Modifier.fillMaxWidth(),
            items = persistentListOf(
                VisualListItemData(
                    message = stringResource(R.string.screen_encryption_reset_bullet_1),
                    iconComposable = {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = CompoundIcons.Check(),
                            contentDescription = null,
                            tint = ElementTheme.colors.iconSuccessPrimary,
                        )
                    },
                ),
                VisualListItemData(
                    message = stringResource(R.string.screen_encryption_reset_bullet_2),
                    iconComposable = {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = CompoundIcons.Info(),
                            contentDescription = null,
                            tint = ElementTheme.colors.iconSecondary,
                        )
                    },
                ),
                VisualListItemData(
                    message = stringResource(R.string.screen_encryption_reset_bullet_3),
                    iconComposable = {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = CompoundIcons.Info(),
                            contentDescription = null,
                            tint = ElementTheme.colors.iconSecondary,
                        )
                    },
                ),
            ),
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.screen_encryption_reset_footer),
            style = ElementTheme.typography.fontBodyMdMedium,
            color = ElementTheme.colors.textActionPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun ResetIdentityRootViewPreview(@PreviewParameter(ResetIdentityRootStatePreviewParam::class) state: ResetIdentityRootState) {
    ElementPreview {
        ResetIdentityRootView(
            state = state,
            onContinue = {},
            onBack = {},
        )
    }
}
