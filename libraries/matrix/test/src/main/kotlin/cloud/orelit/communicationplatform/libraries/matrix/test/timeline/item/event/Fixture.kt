/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.test.timeline.item.event

import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MembershipChange
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID

fun aRoomMembershipContent(
    userId: UserId = A_USER_ID,
    userDisplayName: String? = null,
    change: MembershipChange? = null,
    reason: String? = null,
) = RoomMembershipContent(
    userId = userId,
    userDisplayName = userDisplayName,
    change = change,
    reason = reason,
)
