/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.userprofile.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.libraries.designsystem.components.button.MainActionButton
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.matrix.api.notification.CallIntent
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
fun UserProfileMainActionsSection(
    isCurrentUser: Boolean,
    canCall: Boolean,
    onShareUser: () -> Unit,
    onStartDM: () -> Unit,
    onCall: (CallIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        if (!isCurrentUser) {
            MainActionButton(
                title = stringResource(CommonStrings.action_message),
                imageVector = CompoundIcons.Chat(),
                onClick = onStartDM,
            )
        }
        if (canCall) {
            MainActionButton(
                title = stringResource(CommonStrings.action_call),
                imageVector = CompoundIcons.VoiceCall(),
                onClick = { onCall(CallIntent.AUDIO) },
            )

            MainActionButton(
                title = stringResource(CommonStrings.common_video),
                imageVector = CompoundIcons.VideoCall(),
                onClick = { onCall(CallIntent.VIDEO) },
            )
        }
        MainActionButton(
            title = stringResource(CommonStrings.action_share),
            imageVector = CompoundIcons.ShareAndroid(),
            onClick = onShareUser
        )
    }
}

@PreviewsDayNight()
@Composable
internal fun UserProfileMainActionsSectionPreview() = ElementPreview {
    UserProfileMainActionsSection(
        isCurrentUser = false,
        canCall = true,
        onShareUser = { },
        onStartDM = { },
        onCall = { }
    )
}
