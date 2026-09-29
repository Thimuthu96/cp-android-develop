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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.Avatar
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarType
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getAvatarData
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getBestName

@Composable
fun MatrixUserHeader(
    matrixUser: MatrixUser,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            modifier = Modifier
                .padding(vertical = 7.dp),
            avatarData = matrixUser.getAvatarData(size = AvatarSize.UserPreference),
            avatarType = AvatarType.User,
        )
        Spacer(modifier = Modifier.width(13.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Name
            DisplayNameWithStatus(
                name = matrixUser.getBestName(),
                status = matrixUser.displayedStatus,
                nameColor = ElementTheme.colors.textPrimary,
                style = ElementTheme.typography.fontHeadingMdRegular,
            )
            // Id
            if (matrixUser.displayName.isNullOrEmpty().not()) {
                Text(
                    text = matrixUser.userId.value,
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun MatrixUserHeaderPreview(@PreviewParameter(MatrixUserPreviewParam::class) matrixUser: MatrixUser) = ElementPreview {
    MatrixUserHeader(matrixUser)
}
