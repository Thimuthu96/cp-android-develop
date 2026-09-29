/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomaliasresolver.impl

import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.room.alias.ResolvedRoomAlias

data class RoomAliasResolverState(
    val roomAlias: RoomAlias,
    val resolveState: AsyncData<ResolvedRoomAlias>,
    val eventSink: (RoomAliasResolverEvent) -> Unit
)

sealed class RoomAliasResolverFailures : Exception() {
    data object UnknownAlias : RoomAliasResolverFailures()
}
