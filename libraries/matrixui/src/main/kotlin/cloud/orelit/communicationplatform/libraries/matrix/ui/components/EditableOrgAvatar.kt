/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.Avatar
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarType
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.anAvatarData
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.text.toPx
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Surface
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

/**
 * Ref: https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=3643-2678&m=dev
 */
@Composable
fun EditableOrgAvatar(
    avatarData: AvatarData,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionEdit = stringResource(id = CommonStrings.action_edit)
    val description = stringResource(CommonStrings.a11y_avatar)
    Box(
        modifier = modifier
            .width(avatarData.size.dp + 16.dp)
            .clearAndSetSemantics {
                contentDescription = description
                // Note: this does not set the click effect to the whole Box
                // when talkback is not enabled
                onClick(
                    label = actionEdit,
                    action = {
                        onEdit()
                        true
                    }
                )
            }
    ) {
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val editIconRadius = 17.dp.toPx()
        val editIconXOffset = 7.dp.toPx()
        val editIconYOffset = 15.dp.toPx()
        Avatar(
            avatarData = avatarData,
            avatarType = AvatarType.Space(false),
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()
                    val xOffset = if (isRtl) {
                        editIconXOffset
                    } else {
                        size.width - editIconXOffset
                    }
                    drawCircle(
                        color = Color.Black,
                        center = Offset(
                            x = xOffset,
                            y = size.height - editIconYOffset,
                        ),
                        radius = editIconRadius,
                        blendMode = BlendMode.Clear,
                    )
                },
        )
        Surface(
            color = ElementTheme.colors.bgCanvasDefault,
            shape = CircleShape,
            border = BorderStroke(1.dp, color = ElementTheme.colors.borderInteractiveSecondary),
            modifier = Modifier
                .clip(CircleShape)
                .size(30.dp)
                .align(Alignment.BottomEnd)
                .clickable(
                    indication = ripple(),
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onEdit,
                ),
        ) {
            Icon(
                imageVector = CompoundIcons.Edit(),
                // Note: keep the context description for the test
                contentDescription = stringResource(id = CommonStrings.action_edit),
                tint = ElementTheme.colors.iconPrimary,
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun EditableOrgAvatarPreview() = ElementPreview {
    EditableOrgAvatar(
        avatarData = anAvatarData(
            url = "anUrl",
            size = AvatarSize.OrganizationHeader,
        ),
        onEdit = {},
    )
}

@PreviewsDayNight
@Composable
internal fun EditableOrgAvatarRtlPreview() = CompositionLocalProvider(
    LocalLayoutDirection provides LayoutDirection.Rtl,
) {
    ElementPreview {
        EditableOrgAvatar(
            avatarData = anAvatarData(
                url = "anUrl",
                size = AvatarSize.OrganizationHeader,
            ),
            onEdit = {},
        )
    }
}
