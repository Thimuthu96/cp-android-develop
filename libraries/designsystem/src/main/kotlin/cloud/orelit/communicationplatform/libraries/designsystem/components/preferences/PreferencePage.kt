/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.designsystem.components.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.libraries.designsystem.components.button.BackButton
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.aliasScreenTitle
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Scaffold
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TopAppBar
import cloud.orelit.communicationplatform.libraries.designsystem.utils.lazyColumnContentPadding
import cloud.orelit.communicationplatform.libraries.designsystem.utils.scaffoldScrollableContentInsets

@Composable
fun PreferencePage(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentWindowInsets = scaffoldScrollableContentInsets,
        topBar = {
            PreferenceTopAppBar(
                title = title,
                onBackClick = onBackClick,
            )
        },
        snackbarHost = snackbarHost,
        content = { contentPadding ->
            Column(
                modifier = Modifier
                    .verticalScroll(state = rememberScrollState())
                    .padding(contentPadding + lazyColumnContentPadding)
                    .consumeWindowInsets(contentPadding + lazyColumnContentPadding)
            ) {
                content()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreferenceTopAppBar(
    title: String,
    onBackClick: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            BackButton(onClick = onBackClick)
        },
        title = {
            Text(
                modifier = Modifier.semantics {
                    heading()
                },
                text = title,
                style = ElementTheme.typography.aliasScreenTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

@PreviewsDayNight
@Composable
internal fun PreferencePagePreview() = ElementPreview {
    PreferencePage(
        title = "Preference screen",
        onBackClick = {},
    ) {
        PreferenceCategory(
            title = "Category title",
        ) {
            PreferenceDivider()
            PreferenceSwitch(
                title = "Switch",
                icon = CompoundIcons.Threads(),
                isChecked = true,
                onCheckedChange = {},
            )
            PreferenceDivider()
            PreferenceCheckbox(
                title = "Checkbox",
                icon = CompoundIcons.Notifications(),
                isChecked = true,
                onCheckedChange = {},
            )
            PreferenceDivider()
            PreferenceSlide(
                title = "Slide",
                summary = "Summary",
                value = 0.75F,
                showIconAreaIfNoIcon = true,
                onValueChange = {},
            )
        }
    }
}
