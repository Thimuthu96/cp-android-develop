/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.userprofile.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.userprofile.api.UserProfileVerificationState
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.atoms.MatrixBadgeAtom
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.molecules.MatrixBadgeRowMolecule
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.Avatar
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarType
import cloud.orelit.communicationplatform.libraries.designsystem.modifiers.niceClickable
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.preview.USER_NAME_ALICE
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.ButtonSize
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.OutlinedButton
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.user.DisplayedStatus
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.toText
import cloud.orelit.communicationplatform.libraries.testtags.TestTags
import cloud.orelit.communicationplatform.libraries.testtags.testTag
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList

@Composable
fun UserProfileHeaderSection(
    avatarUrl: String?,
    userId: UserId,
    userName: String?,
    verificationState: UserProfileVerificationState,
    openAvatarPreview: (url: String) -> Unit,
    onUserIdClick: () -> Unit,
    withdrawVerificationClick: () -> Unit,
    displayedStatus: DisplayedStatus?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Avatar(
            avatarData = AvatarData(userId.value, userName, avatarUrl, AvatarSize.UserHeader),
            avatarType = AvatarType.User,
            contentDescription = stringResource(CommonStrings.a11y_user_avatar),
            modifier = Modifier
                .clip(CircleShape)
                .clickable(
                    enabled = avatarUrl != null,
                    onClickLabel = stringResource(CommonStrings.action_view),
                ) {
                    openAvatarPreview(avatarUrl!!)
                }
                .testTag(TestTags.memberDetailAvatar)
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (userName != null) {
            Text(
                modifier = Modifier
                    .clipToBounds()
                    .semantics {
                        heading()
                    },
                text = userName,
                style = ElementTheme.typography.fontHeadingLgBold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        displayedStatus?.let { status ->
            Text(
                text = status.toText(),
                style = ElementTheme.typography.fontBodyLgMedium,
                color = ElementTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        Text(
            modifier = Modifier.niceClickable { onUserIdClick() },
            text = userId.value,
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        when (verificationState) {
            UserProfileVerificationState.UNKNOWN, UserProfileVerificationState.UNVERIFIED -> Unit
            UserProfileVerificationState.VERIFIED -> {
                MatrixBadgeRowMolecule(
                    data = listOf(
                        MatrixBadgeAtom.MatrixBadgeData(
                            text = stringResource(CommonStrings.common_verified),
                            icon = CompoundIcons.Verified(),
                            type = MatrixBadgeAtom.Type.Positive,
                        )
                    ).toImmutableList(),
                )
            }
            UserProfileVerificationState.VERIFICATION_VIOLATION -> {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(CommonStrings.crypto_identity_change_profile_pin_violation, userName ?: userId.value),
                    color = ElementTheme.colors.textCriticalPrimary,
                    style = ElementTheme.typography.fontBodyMdMedium,
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    size = ButtonSize.MediumLowPadding,
                    text = stringResource(CommonStrings.crypto_identity_change_withdraw_verification_action),
                    onClick = withdrawVerificationClick,
                )
            }
        }
        Spacer(Modifier.height(40.dp))
    }
}

@PreviewsDayNight
@Composable
internal fun UserProfileHeaderSectionPreview() = ElementPreview {
    UserProfileHeaderSection(
        avatarUrl = null,
        userId = UserId("@alice:example.com"),
        userName = USER_NAME_ALICE,
        verificationState = UserProfileVerificationState.VERIFIED,
        displayedStatus = DisplayedStatus.InCall(0L),
        openAvatarPreview = {},
        onUserIdClick = {},
        withdrawVerificationClick = {},
    )
}

@PreviewsDayNight
@Composable
internal fun UserProfileHeaderSectionWithVerificationViolationPreview() = ElementPreview {
    UserProfileHeaderSection(
        avatarUrl = null,
        userId = UserId("@alice:example.com"),
        userName = USER_NAME_ALICE,
        verificationState = UserProfileVerificationState.VERIFICATION_VIOLATION,
        displayedStatus = DisplayedStatus.InCall(0L),
        openAvatarPreview = {},
        onUserIdClick = {},
        withdrawVerificationClick = {},
    )
}
