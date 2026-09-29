/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.invite.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import cloud.orelit.communicationplatform.features.invite.api.SeenInvitesStore
import cloud.orelit.communicationplatform.features.invite.api.acceptdecline.AcceptDeclineInviteState
import cloud.orelit.communicationplatform.features.invite.impl.SeenInvitesStoreFactory
import cloud.orelit.communicationplatform.features.invite.impl.acceptdecline.AcceptDeclineInvitePresenter
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient

@ContributesTo(SessionScope::class)
@BindingContainer
interface InviteBindingContainer {
    @Binds
    fun bindAcceptDeclineInvitePresenter(presenter: AcceptDeclineInvitePresenter): Presenter<AcceptDeclineInviteState>

    companion object {
        @Provides
        fun providesSeenInvitesStore(
            factory: SeenInvitesStoreFactory,
            matrixClient: MatrixClient,
        ): SeenInvitesStore {
            return factory.getOrCreate(
                matrixClient.sessionId,
                matrixClient.sessionCoroutineScope,
            )
        }
    }
}
