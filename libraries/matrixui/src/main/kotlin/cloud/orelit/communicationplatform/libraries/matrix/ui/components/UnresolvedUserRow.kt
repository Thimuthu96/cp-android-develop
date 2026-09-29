/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.Avatar
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarType
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementThemedPreview
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getAvatarData
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
fun UnresolvedUserRow(
    avatarData: AvatarData,
    id: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            avatarData = avatarData,
            avatarType = AvatarType.User,
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // ID
            Text(
                text = id,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (enabled) ElementTheme.colors.textPrimary else ElementTheme.colors.textDisabled,
                style = ElementTheme.typography.fontBodyLgMedium,
            )

            // Warning
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp)
            ) {
                Icon(
                    imageVector = CompoundIcons.ErrorSolid(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.Top)
                        .padding(2.dp),
                    tint = if (enabled) ElementTheme.colors.iconCriticalPrimary else ElementTheme.colors.iconDisabled,
                )
                Text(
                    text = stringResource(CommonStrings.common_invite_unknown_profile),
                    color = if (enabled) ElementTheme.colors.textSecondary else ElementTheme.colors.textDisabled,
                    style = ElementTheme.typography.fontBodySmRegular.copy(lineHeight = 16.sp),
                )
            }
        }
    }
}

@Preview
@Composable
internal fun UnresolvedUserRowPreview() = ElementThemedPreview {
    val matrixUser = aMatrixUser()
    Column {
        UnresolvedUserRow(matrixUser.getAvatarData(size = AvatarSize.UserListItem), matrixUser.userId.value)
        UnresolvedUserRow(matrixUser.getAvatarData(size = AvatarSize.UserListItem), matrixUser.userId.value, enabled = false)
    }
}
