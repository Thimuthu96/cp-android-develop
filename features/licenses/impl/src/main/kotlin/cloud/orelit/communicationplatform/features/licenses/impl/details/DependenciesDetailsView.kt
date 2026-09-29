/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.licenses.impl.details

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cloud.orelit.communicationplatform.features.licenses.impl.list.aDependencyLicenseItem
import cloud.orelit.communicationplatform.features.licenses.impl.model.DependencyLicenseItem
import cloud.orelit.communicationplatform.libraries.designsystem.components.ClickableLinkText
import cloud.orelit.communicationplatform.libraries.designsystem.components.button.BackButton
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ListItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Scaffold
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TopAppBar
import cloud.orelit.communicationplatform.libraries.designsystem.utils.lazyColumnContentPadding
import cloud.orelit.communicationplatform.libraries.designsystem.utils.scaffoldScrollableContentInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DependenciesDetailsView(
    licenseItem: DependencyLicenseItem,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                titleStr = licenseItem.safeName,
                navigationIcon = { BackButton(onClick = onBack) },
            )
        },
        contentWindowInsets = scaffoldScrollableContentInsets,
    ) { contentPadding ->
        LazyColumn(
            contentPadding = contentPadding + lazyColumnContentPadding,
        ) {
            val licenses = licenseItem.licenses.orEmpty() +
                licenseItem.unknownLicenses.orEmpty()
            items(licenses) { license ->
                val text = buildString {
                    if (license.name != null) {
                        append(license.name)
                        append("\n")
                        append("\n")
                    }
                    if (license.url != null) {
                        append(license.url)
                    }
                }
                ListItem(
                    content = {
                        ClickableLinkText(
                            text = text,
                            interactionSource = remember { MutableInteractionSource() },
                        )
                    }
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun DependenciesDetailsViewPreview() = ElementPreview {
    DependenciesDetailsView(
        licenseItem = aDependencyLicenseItem(),
        onBack = {}
    )
}
