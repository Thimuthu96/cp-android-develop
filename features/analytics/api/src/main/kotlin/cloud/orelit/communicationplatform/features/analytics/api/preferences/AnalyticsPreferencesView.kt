/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.analytics.api.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import cloud.orelit.communicationplatform.features.analytics.api.AnalyticsOptInEvent
import cloud.orelit.communicationplatform.features.analytics.api.R
import cloud.orelit.communicationplatform.libraries.designsystem.components.LINK_TAG
import cloud.orelit.communicationplatform.libraries.designsystem.components.list.ListItemContent
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.text.buildAnnotatedStringWithStyledPart
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListSupportingText
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text

@Composable
fun AnalyticsPreferencesView(
    state: AnalyticsPreferencesState,
    modifier: Modifier = Modifier,
) {
    fun onEnabledChanged(isEnabled: Boolean) {
        state.eventSink(AnalyticsOptInEvent.EnableAnalytics(isEnabled = isEnabled))
    }

    val supportingText = stringResource(
        id = R.string.screen_analytics_settings_help_us_improve,
        state.applicationName
    )
    Column(modifier) {
        ListItem(
            content = {
                Text(stringResource(id = R.string.screen_analytics_settings_share_data))
            },
            supportingContent = {
                Text(supportingText)
            },
            leadingContent = null,
            trailingContent = ListItemContent.Switch(
                checked = state.isEnabled,
            ),
            onClick = {
                onEnabledChanged(!state.isEnabled)
            }
        )
        if (state.policyUrl.isNotEmpty()) {
            val linkText = buildAnnotatedStringWithStyledPart(
                R.string.screen_analytics_settings_read_terms,
                R.string.screen_analytics_settings_read_terms_content_link,
                tagAndLink = LINK_TAG to state.policyUrl,
            )
            ListSupportingText(annotatedString = linkText)
        }
    }
}

@PreviewsDayNight
@Composable
internal fun AnalyticsPreferencesViewPreview(@PreviewParameter(AnalyticsPreferencesStatePreviewParam::class) state: AnalyticsPreferencesState) =
    ElementPreview {
        AnalyticsPreferencesView(
            state = state,
        )
    }
