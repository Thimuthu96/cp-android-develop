/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.threads.list

import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import kotlinx.collections.immutable.ImmutableList

data class ThreadsListState(
    val roomId: RoomId,
    val roomName: String,
    val roomAvatarUrl: String?,
    val isRoomTombstoned: Boolean,
    val heroes: ImmutableList<AvatarData>,
    val threads: ImmutableList<ThreadListRowItem>,
    val eventSink: (ThreadsListEvent) -> Unit,
)
