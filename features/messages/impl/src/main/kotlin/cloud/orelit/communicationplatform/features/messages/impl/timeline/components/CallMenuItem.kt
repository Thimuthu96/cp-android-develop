/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.roomcall.api.RoomCallState
import cloud.orelit.communicationplatform.features.roomcall.api.RoomCallStatePreviewParam
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.IconButton
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings

@Composable
internal fun CallMenuItem(
    roomCallState: RoomCallState,
    onJoinCallClick: (isAudioCall: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (roomCallState) {
        RoomCallState.Unavailable -> {
            Box(modifier)
        }
        is RoomCallState.StandBy -> {
            StandByCallMenuItem(
                roomCallState = roomCallState,
                onJoinCallClick = onJoinCallClick,
                modifier = modifier,
            )
        }
        is RoomCallState.OnGoing -> {
            OnGoingCallMenuItem(
                roomCallState = roomCallState,
                onJoinCallClick = { onJoinCallClick(roomCallState.isAudioCall) },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun StandByCallMenuItem(
    roomCallState: RoomCallState.StandBy,
    onJoinCallClick: (isAudioCall: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        // Only show voice call in DMs
        if (roomCallState.isDM) {
            IconButton(
                onClick = { onJoinCallClick(true) },
                enabled = roomCallState.canStartCall,
            ) {
                Icon(
                    imageVector = CompoundIcons.VoiceCallSolid(),
                    contentDescription = stringResource(CommonStrings.a11y_start_voice_call),
                )
            }
        }
        IconButton(
            onClick = { onJoinCallClick(false) },
            enabled = roomCallState.canStartCall,
        ) {
            Icon(
                imageVector = CompoundIcons.VideoCallSolid(),
                contentDescription = stringResource(CommonStrings.a11y_start_call),
            )
        }
    }
}

@Composable
private fun OnGoingCallMenuItem(
    roomCallState: RoomCallState.OnGoing,
    onJoinCallClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!roomCallState.isUserLocallyInTheCall) {
        Button(
            onClick = onJoinCallClick,
            colors = ButtonDefaults.buttonColors(
                contentColor = ElementTheme.colors.bgCanvasDefault,
                containerColor = ElementTheme.colors.iconAccentTertiary
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
            modifier = modifier.heightIn(min = 36.dp),
            enabled = roomCallState.canJoinCall,
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = if (roomCallState.isAudioCall) {
                    CompoundIcons.VoiceCallSolid()
                } else {
                    CompoundIcons.VideoCallSolid()
                },
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(CommonStrings.action_join),
                style = ElementTheme.typography.fontBodyMdMedium
            )
            Spacer(Modifier.width(8.dp))
        }
    } else {
        // Else user is already in the call, hide the button.
        Box(modifier)
    }
}

@PreviewsDayNight
@Composable
internal fun CallMenuItemPreview(
    @PreviewParameter(RoomCallStatePreviewParam::class) roomCallState: RoomCallState
) = ElementPreview {
    CallMenuItem(
        roomCallState = roomCallState,
        onJoinCallClick = {}
    )
}
