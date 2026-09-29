/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.notifications.edit

import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.matrix.api.room.RoomNotificationMode
import kotlinx.collections.immutable.ImmutableList

data class EditDefaultNotificationSettingState(
    val isOneToOne: Boolean,
    val mode: RoomNotificationMode?,
    val roomsWithUserDefinedMode: ImmutableList<EditNotificationSettingRoomInfo>,
    val changeNotificationSettingAction: AsyncAction<Unit>,
    val displayMentionsOnlyDisclaimer: Boolean,
    val eventSink: (EditDefaultNotificationSettingStateEvent) -> Unit,
)
