/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.troubleshoot.impl

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.designsystem.components.list.ListItemContent
import cloud.orelit.communicationplatform.libraries.designsystem.components.preferences.PreferencePage
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Button
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.CircularProgressIndicator
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.designsystem.utils.OnLifecycleEvent
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.test.NotificationTroubleshootTestState.Status

@Composable
fun TroubleshootNotificationsView(
    state: TroubleshootNotificationsState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (state.hasFailedTests) {
                    state.eventSink(TroubleshootNotificationsEvent.RetryFailedTests)
                }
            }
            else -> Unit
        }
    }

    PreferencePage(
        modifier = modifier,
        onBackClick = onBackClick,
        title = stringResource(id = R.string.troubleshoot_notifications_screen_title),
    ) {
        TroubleshootNotificationsContent(state)
    }
}

@Composable
private fun ColumnScope.TroubleshootTestView(
    testState: NotificationTroubleshootTestState,
    onQuickFixClick: () -> Unit,
) {
    val status = testState.status
    if ((status as? Status.Idle)?.visible == false) return
    ListItem(
        content = { Text(text = testState.name) },
        supportingContent = { Text(text = testState.description) },
        trailingContent = when (status) {
            is Status.Idle -> null
            Status.InProgress -> ListItemContent.Custom {
                CircularProgressIndicator(
                    modifier = Modifier
                        .progressSemantics()
                        .size(20.dp),
                    strokeWidth = 2.dp
                )
            }
            Status.WaitingForUser -> ListItemContent.Custom {
                Icon(
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    imageVector = CompoundIcons.Info(),
                    tint = ElementTheme.colors.iconAccentTertiary
                )
            }
            Status.Success -> ListItemContent.Custom {
                Icon(
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    imageVector = CompoundIcons.Check(),
                    tint = ElementTheme.colors.iconAccentTertiary
                )
            }
            is Status.Failure -> ListItemContent.Custom {
                Icon(
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    imageVector = if (status.isCritical) CompoundIcons.ErrorSolid() else CompoundIcons.Warning(),
                    tint = ElementTheme.colors.iconCriticalPrimary,
                )
            }
        }
    )
    if (status is Status.Failure && status.hasQuickFix) {
        ListItem(
            content = { },
            trailingContent = ListItemContent.Custom {
                Button(
                    text = status.quickFixButtonString ?: stringResource(id = R.string.troubleshoot_notifications_screen_quick_fix_action),
                    onClick = onQuickFixClick,
                )
            }
        )
    }
}

@Composable
private fun ColumnScope.TroubleshootNotificationsContent(state: TroubleshootNotificationsState) {
    when (state.testSuiteState.mainState) {
        AsyncAction.Loading,
        is AsyncAction.Confirming,
        is AsyncAction.Success,
        is AsyncAction.Failure -> {
            TestSuiteView(
                testSuiteState = state.testSuiteState,
                onQuickFixClick = {
                    state.eventSink(TroubleshootNotificationsEvent.QuickFix(it))
                }
            )
        }
        AsyncAction.Uninitialized -> Unit
    }
    when (state.testSuiteState.mainState) {
        AsyncAction.Uninitialized -> {
            ListItem(content = {
                Text(
                    text = stringResource(id = R.string.troubleshoot_notifications_screen_notice)
                )
            })
            RunTestButton(state = state)
        }
        AsyncAction.Loading -> Unit
        is AsyncAction.Failure -> {
            ListItem(content = {
                Text(text = stringResource(id = R.string.troubleshoot_notifications_screen_failure))
            })
            RunTestButton(state = state)
        }
        is AsyncAction.Confirming -> {
            ListItem(content = {
                Text(
                    text = stringResource(id = R.string.troubleshoot_notifications_screen_waiting)
                )
            })
        }
        is AsyncAction.Success -> {
            ListItem(content = {
                Text(
                    text = stringResource(id = R.string.troubleshoot_notifications_screen_success)
                )
            })
        }
    }
}

@Composable
private fun RunTestButton(state: TroubleshootNotificationsState) {
    ListItem(
        content = {
            Button(
                text = stringResource(
                    id = if (state.testSuiteState.mainState is AsyncAction.Failure) {
                        R.string.troubleshoot_notifications_screen_action_again
                    } else {
                        R.string.troubleshoot_notifications_screen_action
                    }
                ),
                onClick = {
                    state.eventSink(TroubleshootNotificationsEvent.StartTests)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    )
}

@Composable
private fun ColumnScope.TestSuiteView(
    testSuiteState: TroubleshootTestSuiteState,
    onQuickFixClick: (Int) -> Unit,
) {
    testSuiteState.tests.forEachIndexed { index, testState ->
        TroubleshootTestView(
            testState = testState,
            onQuickFixClick = {
                onQuickFixClick(index)
            },
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TroubleshootNotificationsViewPreview(
    @PreviewParameter(TroubleshootNotificationsStatePreviewParam::class) state: TroubleshootNotificationsState,
) = ElementPreview {
    TroubleshootNotificationsView(
        state = state,
        onBackClick = {},
    )
}
