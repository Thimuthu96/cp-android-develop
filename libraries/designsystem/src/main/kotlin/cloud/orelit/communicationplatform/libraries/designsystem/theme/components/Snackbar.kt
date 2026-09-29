/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.designsystem.theme.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.theme.SnackBarLabelColorDark
import cloud.orelit.communicationplatform.compound.theme.SnackBarLabelColorLight
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.libraries.designsystem.components.button.ButtonVisuals
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementThemedPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewGroup

@Composable
fun Snackbar(
    message: String,
    modifier: Modifier = Modifier,
    action: ButtonVisuals? = null,
    dismissAction: ButtonVisuals? = null,
    actionOnNewLine: Boolean = false,
    shape: Shape = RoundedCornerShape(8.dp),
    containerColor: Color = SnackbarDefaults.color,
    contentColor: Color = ElementTheme.materialColors.inverseOnSurface,
    actionContentColor: Color = actionContentColor(),
    dismissActionContentColor: Color = SnackbarDefaults.dismissActionContentColor,
) {
    Snackbar(
        modifier = modifier,
        action = action?.let { @Composable { it.Composable() } },
        dismissAction = dismissAction?.let { @Composable { it.Composable() } },
        actionOnNewLine = actionOnNewLine,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        actionContentColor = actionContentColor,
        dismissActionContentColor = dismissActionContentColor,
        content = { Text(text = message) },
    )
}

@Composable
fun Snackbar(
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
    dismissAction: @Composable (() -> Unit)? = null,
    actionOnNewLine: Boolean = false,
    shape: Shape = RoundedCornerShape(8.dp),
    containerColor: Color = SnackbarDefaults.color,
    contentColor: Color = ElementTheme.materialColors.inverseOnSurface,
    actionContentColor: Color = actionContentColor(),
    dismissActionContentColor: Color = SnackbarDefaults.dismissActionContentColor,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.Snackbar(
        modifier = modifier,
        action = action,
        dismissAction = dismissAction,
        actionOnNewLine = actionOnNewLine,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        actionContentColor = actionContentColor,
        dismissActionContentColor = dismissActionContentColor,
        content = content,
    )
}

// TODO this color is temporary, an `inverse` version should be added to the semantic colors instead
@Composable
private fun actionContentColor(): Color {
    return if (ElementTheme.isLightTheme) {
        SnackBarLabelColorLight
    } else {
        SnackBarLabelColorDark
    }
}

@Preview(name = "Snackbar", group = PreviewGroup.Snackbars)
@Composable
internal fun SnackbarPreview() {
    ElementThemedPreview {
        Snackbar(message = "Snackbar supporting text")
    }
}

@Preview(name = "Snackbar with action", group = PreviewGroup.Snackbars)
@Composable
internal fun SnackbarWithActionPreview() {
    ElementThemedPreview {
        Snackbar(message = "Snackbar supporting text", action = ButtonVisuals.Text("Action", {}))
    }
}

@Preview(name = "Snackbar with action and close button", group = PreviewGroup.Snackbars)
@Composable
internal fun SnackbarWithActionAndCloseButtonPreview() {
    ElementThemedPreview {
        Snackbar(
            message = "Snackbar supporting text",
            action = ButtonVisuals.Text("Action") {},
            dismissAction = ButtonVisuals.Icon(
                IconSource.Vector(CompoundIcons.Close())
            ) {}
        )
    }
}

@Preview(name = "Snackbar with action on new line", group = PreviewGroup.Snackbars)
@Composable
internal fun SnackbarWithActionOnNewLinePreview() {
    ElementThemedPreview {
        Snackbar(message = "Snackbar supporting text", action = ButtonVisuals.Text("Action", {}), actionOnNewLine = true)
    }
}

@Preview(name = "Snackbar with action and close button on new line", group = PreviewGroup.Snackbars)
@Composable
internal fun SnackbarWithActionOnNewLineAndCloseButtonPreview() {
    ElementThemedPreview {
        Snackbar(
            message = "Snackbar supporting text",
            action = ButtonVisuals.Text("Action", {}),
            dismissAction = ButtonVisuals.Icon(
                IconSource.Vector(CompoundIcons.Close())
            ) {},
            actionOnNewLine = true
        )
    }
}
