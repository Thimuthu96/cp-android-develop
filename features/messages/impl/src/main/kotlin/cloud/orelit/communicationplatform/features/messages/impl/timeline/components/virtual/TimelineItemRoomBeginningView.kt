/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.timeline.components.virtual

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.features.messages.impl.R
import cloud.orelit.communicationplatform.libraries.designsystem.atomic.molecules.ComposerAlertMolecule
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ROOM_NAME
import cloud.orelit.communicationplatform.libraries.designsystem.text.toAnnotatedString
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.designsystem.utils.allBooleans
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.tombstone.PredecessorRoom

@Composable
fun TimelineItemRoomBeginningView(
    roomName: String?,
    predecessorRoom: PredecessorRoom?,
    isDm: Boolean,
    onPredecessorRoomClick: (RoomId) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        if (predecessorRoom != null) {
            ComposerAlertMolecule(
                avatar = null,
                content = stringResource(R.string.screen_room_timeline_upgraded_room_message).toAnnotatedString(),
                onSubmitClick = { onPredecessorRoomClick(predecessorRoom.roomId) },
                submitText = stringResource(R.string.screen_room_timeline_upgraded_room_action)
            )
        }
        // Only display for non-DM room
        if (!isDm) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                val text = if (roomName == null) {
                    stringResource(id = R.string.screen_room_timeline_beginning_of_room_no_name)
                } else {
                    stringResource(id = R.string.screen_room_timeline_beginning_of_room, roomName)
                }
                Text(
                    color = ElementTheme.colors.textSecondary,
                    style = ElementTheme.typography.fontBodyMdRegular,
                    text = text,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemRoomBeginningViewPreview() = ElementPreview {
    Column(verticalArrangement = spacedBy(16.dp)) {
        allBooleans.forEach { isDm ->
            TimelineItemRoomBeginningView(
                predecessorRoom = null,
                roomName = null,
                isDm = isDm,
                onPredecessorRoomClick = {},
            )
            TimelineItemRoomBeginningView(
                predecessorRoom = null,
                roomName = ROOM_NAME,
                isDm = isDm,
                onPredecessorRoomClick = {},
            )
            TimelineItemRoomBeginningView(
                predecessorRoom = PredecessorRoom(RoomId("!roomId:matrix.org")),
                roomName = ROOM_NAME,
                isDm = isDm,
                onPredecessorRoomClick = {},
            )
        }
    }
}
