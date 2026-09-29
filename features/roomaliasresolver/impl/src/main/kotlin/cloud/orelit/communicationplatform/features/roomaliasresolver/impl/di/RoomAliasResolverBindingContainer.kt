/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomaliasresolver.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import cloud.orelit.communicationplatform.features.roomaliasresolver.impl.RoomAliasResolverPresenter
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomAlias

@BindingContainer
@ContributesTo(SessionScope::class)
object RoomAliasResolverBindingContainer {
    @Provides
    fun providesRoomAliasResolverPresenterFactory(
        client: MatrixClient,
    ): RoomAliasResolverPresenter.Factory {
        return object : RoomAliasResolverPresenter.Factory {
            override fun create(roomAlias: RoomAlias): RoomAliasResolverPresenter {
                return RoomAliasResolverPresenter(
                    roomAlias = roomAlias,
                    matrixClient = client,
                )
            }
        }
    }
}
