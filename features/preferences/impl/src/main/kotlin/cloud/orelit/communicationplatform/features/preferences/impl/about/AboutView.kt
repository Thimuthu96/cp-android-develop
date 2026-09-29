/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.about

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import cloud.orelit.communicationplatform.libraries.designsystem.components.preferences.PreferencePage
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
fun AboutView(
    state: AboutState,
    onElementLegalClick: (ElementLegal) -> Unit,
    onOpenSourceLicensesClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferencePage(
        modifier = modifier,
        onBackClick = onBackClick,
        title = stringResource(id = CommonStrings.common_about)
    ) {
        state.elementLegals.forEach { elementLegal ->
            ListItem(
                content = {
                    Text(stringResource(id = elementLegal.titleRes))
                },
                onClick = { onElementLegalClick(elementLegal) }
            )
        }
        ListItem(
            content = {
                Text(stringResource(id = CommonStrings.common_open_source_licenses))
            },
            onClick = onOpenSourceLicensesClick,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun AboutViewPreview(@PreviewParameter(AboutStatePreviewParam::class) state: AboutState) = ElementPreview {
    AboutView(
        state = state,
        onElementLegalClick = {},
        onOpenSourceLicensesClick = {},
        onBackClick = {},
    )
}
