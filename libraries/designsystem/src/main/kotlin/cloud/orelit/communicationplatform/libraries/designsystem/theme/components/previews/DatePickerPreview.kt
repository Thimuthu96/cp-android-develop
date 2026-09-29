/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.designsystem.theme.components.previews

import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.libraries.architecture.coverage.ExcludeFromCoverage
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreviewDark
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreviewLight
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewGroup
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.AlertDialogContent

@Preview(group = PreviewGroup.DateTimePickers)
@Composable
internal fun DatePickerLightPreview() {
    ElementPreviewLight { ContentToPreview() }
}

@Preview(group = PreviewGroup.DateTimePickers)
@Composable
internal fun DatePickerDarkPreview() {
    ElementPreviewDark { ContentToPreview() }
}

@OptIn(ExperimentalMaterial3Api::class)
@ExcludeFromCoverage
@Composable
private fun ContentToPreview() {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = 1_672_578_000_000L,
    )
    AlertDialogContent(
        buttons = { /*TODO*/ },
        icon = { /*TODO*/ },
        title = { /*TODO*/ },
        subtitle = null,
        content = { DatePicker(state = state, showModeToggle = true) },
        shape = AlertDialogDefaults.shape,
        containerColor = AlertDialogDefaults.containerColor,
        tonalElevation = AlertDialogDefaults.TonalElevation,
        buttonContentColor = ElementTheme.colors.textPrimary,
        iconContentColor = AlertDialogDefaults.iconContentColor,
        titleContentColor = AlertDialogDefaults.titleContentColor,
        textContentColor = AlertDialogDefaults.textContentColor,
    )
}
