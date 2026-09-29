/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.designsystem.components.async

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.CircularProgressIndicator
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon

/**
 * A helper to create [AsyncIndicatorView] with some defaults.
 */
@Stable
object AsyncIndicator {
    /**
     * A loading async indicator.
     * @param text The text to display.
     * @param modifier The modifier to apply to the indicator.
     */
    @Composable
    fun Loading(
        text: String,
        modifier: Modifier = Modifier,
    ) {
        AsyncIndicatorView(
            modifier = modifier,
            text = text,
            spacing = 10.dp,
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .progressSemantics()
                    .size(12.dp),
                color = ElementTheme.colors.textPrimary,
                strokeWidth = 1.5.dp,
            )
        }
    }

    /**
     * A failure async indicator.
     * @param text The text to display.
     * @param modifier The modifier to apply to the indicator.
     */
    @Composable
    fun Failure(
        text: String,
        modifier: Modifier = Modifier,
    ) {
        AsyncIndicatorView(
            modifier = modifier,
            text = text,
            spacing = defaultSpacing
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = CompoundIcons.Close(),
                contentDescription = null,
            )
        }
    }

    /**
     * A custom async indicator.
     * @param text The text to display.
     * @param modifier The modifier to apply to the indicator.
     * @param spacing The spacing between the leading content and the text.
     * @param leadingContent The leading content to display.
     */
    @Composable
    fun Custom(
        text: String,
        modifier: Modifier = Modifier,
        spacing: Dp = defaultSpacing,
        leadingContent: @Composable (() -> Unit)? = null,
    ) {
        AsyncIndicatorView(
            modifier = modifier,
            text = text,
            spacing = spacing,
            leadingContent = leadingContent,
        )
    }

    /**
     * A short duration to display indicators.
     */
    const val DURATION_SHORT = 3000L

    /**
     * A long duration to display indicators.
     */
    const val DURATION_LONG = 5000L

    private val defaultSpacing = 4.dp
}
