/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.notifications.edit

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomNotificationMode
import kotlinx.collections.immutable.persistentListOf

open class EditDefaultNotificationSettingStatePreviewParam : PreviewParameterProvider<EditDefaultNotificationSettingState> {
    override val values: Sequence<EditDefaultNotificationSettingState>
        get() = sequenceOf(
            anEditDefaultNotificationSettingsState(),
            anEditDefaultNotificationSettingsState(isOneToOne = true),
            anEditDefaultNotificationSettingsState(changeNotificationSettingAction = AsyncAction.Loading),
            anEditDefaultNotificationSettingsState(changeNotificationSettingAction = AsyncAction.Failure(RuntimeException("error"))),
            anEditDefaultNotificationSettingsState(displayMentionsOnlyDisclaimer = true),
        )
}

private fun anEditDefaultNotificationSettingsState(
    isOneToOne: Boolean = false,
    changeNotificationSettingAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    displayMentionsOnlyDisclaimer: Boolean = false,
) = EditDefaultNotificationSettingState(
    isOneToOne = isOneToOne,
    mode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
    roomsWithUserDefinedMode = persistentListOf(
        anEditNotificationSettingRoomInfo("Room"),
        anEditNotificationSettingRoomInfo(null),
    ),
    changeNotificationSettingAction = changeNotificationSettingAction,
    displayMentionsOnlyDisclaimer = displayMentionsOnlyDisclaimer,
    eventSink = {}
)

private fun anEditNotificationSettingRoomInfo(
    name: String?,
) = EditNotificationSettingRoomInfo(
    roomId = RoomId("!roomId:domain"),
    name = name,
    avatarData = AvatarData(
        id = "!roomId:domain",
        name = name,
        url = null,
        size = AvatarSize.CustomRoomNotificationSetting,
    ),
    heroesAvatar = persistentListOf(),
    notificationMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
)
