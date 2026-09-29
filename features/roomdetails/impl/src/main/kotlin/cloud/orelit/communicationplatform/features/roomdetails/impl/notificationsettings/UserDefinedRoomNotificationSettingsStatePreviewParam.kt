/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomdetails.impl.notificationsettings

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.features.roomdetails.impl.aRoomNotificationSettings
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomNotificationMode

internal class UserDefinedRoomNotificationSettingsStatePreviewParam : PreviewParameterProvider<RoomNotificationSettingsState> {
    override val values: Sequence<RoomNotificationSettingsState>
        get() = sequenceOf(
            RoomNotificationSettingsState(
                showUserDefinedSettingStyle = false,
                roomName = "Room 1",
                AsyncData.Success(
                    aRoomNotificationSettings(
                        mode = RoomNotificationMode.MUTE,
                        isDefault = false
                    )
                ),
                pendingRoomNotificationMode = null,
                pendingSetDefault = null,
                defaultRoomNotificationMode = RoomNotificationMode.ALL_MESSAGES,
                setNotificationSettingAction = AsyncAction.Uninitialized,
                restoreDefaultAction = AsyncAction.Uninitialized,
                displayMentionsOnlyDisclaimer = false,
                eventSink = { },
            ),
        )
}
