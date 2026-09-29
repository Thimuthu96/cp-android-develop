/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomaliasresolver.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.architecture.runCatchingUpdatingState
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.room.alias.ResolvedRoomAlias
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeoutException
import kotlin.jvm.optionals.getOrElse
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class RoomAliasResolverPresenter(
    @Assisted private val roomAlias: RoomAlias,
    private val matrixClient: MatrixClient,
) : Presenter<RoomAliasResolverState> {
    fun interface Factory {
        fun create(
            roomAlias: RoomAlias,
        ): RoomAliasResolverPresenter
    }

    @Composable
    override fun present(): RoomAliasResolverState {
        val coroutineScope = rememberCoroutineScope()
        val resolveState: MutableState<AsyncData<ResolvedRoomAlias>> = remember { mutableStateOf(AsyncData.Uninitialized) }
        LaunchedEffect(Unit) {
            resolveAlias(resolveState)
        }

        fun handleEvent(event: RoomAliasResolverEvent) {
            when (event) {
                RoomAliasResolverEvent.Retry -> coroutineScope.resolveAlias(resolveState)
                RoomAliasResolverEvent.DismissError -> resolveState.value = AsyncData.Uninitialized
            }
        }

        return RoomAliasResolverState(
            roomAlias = roomAlias,
            resolveState = resolveState.value,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.resolveAlias(resolveState: MutableState<AsyncData<ResolvedRoomAlias>>) = launch {
        suspend {
            val result = withTimeoutOrNull(ALIAS_RESOLVE_TIMEOUT_IN_SECONDS.seconds) {
                matrixClient.resolveRoomAlias(roomAlias)
            } ?: throw TimeoutException("Timed out while resolving ${roomAlias.value}")
            result
                .getOrThrow()
                .getOrElse { throw RoomAliasResolverFailures.UnknownAlias }
        }.runCatchingUpdatingState(resolveState)
    }
}

private const val ALIAS_RESOLVE_TIMEOUT_IN_SECONDS = 10
