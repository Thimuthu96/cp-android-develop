/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomaliasresolver.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.exception.ClientException
import cloud.orelit.communicationplatform.libraries.matrix.api.room.alias.ResolvedRoomAlias

open class RoomAliasResolverStatePreviewParam : PreviewParameterProvider<RoomAliasResolverState> {
    override val values: Sequence<RoomAliasResolverState>
        get() = sequenceOf(
            aRoomAliasResolverState(),
            aRoomAliasResolverState(
                resolveState = AsyncData.Failure(ClientException.Generic("Something went wrong", null)),
            ),
            aRoomAliasResolverState(
                resolveState = AsyncData.Failure(RoomAliasResolverFailures.UnknownAlias),
            ),
        )
}

fun aRoomAliasResolverState(
    roomAlias: RoomAlias = A_ROOM_ALIAS,
    resolveState: AsyncData<ResolvedRoomAlias> = AsyncData.Uninitialized,
    eventSink: (RoomAliasResolverEvent) -> Unit = {}
) = RoomAliasResolverState(
    roomAlias = roomAlias,
    resolveState = resolveState,
    eventSink = eventSink,
)

private val A_ROOM_ALIAS = RoomAlias("#exa:matrix.org")
