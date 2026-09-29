/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomMember
import cloud.orelit.communicationplatform.libraries.matrix.ui.R

data class InviteSender(
    val userId: UserId,
    val displayName: String,
    val avatarData: AvatarData,
    val membershipChangeReason: String?,
) {
    @Composable
    fun annotatedString(): AnnotatedString {
        return stringResource(R.string.screen_invites_invited_you, displayName, userId.value).let { text ->
            val senderNameStart = stringResource(R.string.screen_invites_invited_you).indexOf($$"%1$s")
            AnnotatedString(
                text = text,
                spanStyles = listOf(
                    AnnotatedString.Range(
                        SpanStyle(
                            fontWeight = FontWeight.Medium,
                            color = ElementTheme.colors.textPrimary
                        ),
                        start = senderNameStart,
                        end = senderNameStart + displayName.length
                    )
                )
            )
        }
    }
}

fun RoomMember.toInviteSender() = InviteSender(
    userId = userId,
    displayName = displayName ?: "",
    avatarData = getAvatarData(size = AvatarSize.InviteSender),
    membershipChangeReason = membershipChangeReason
)
