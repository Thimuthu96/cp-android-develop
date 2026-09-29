/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.knockrequests.impl.list

import androidx.compose.runtime.Immutable
import cloud.orelit.communicationplatform.features.knockrequests.api.KnockRequestPermissions
import cloud.orelit.communicationplatform.features.knockrequests.impl.data.KnockRequestPresentable
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import kotlinx.collections.immutable.ImmutableList

data class KnockRequestsListState(
    val knockRequests: AsyncData<ImmutableList<KnockRequestPresentable>>,
    val currentAction: KnockRequestsAction,
    val asyncAction: AsyncAction<Unit>,
    val permissions: KnockRequestPermissions,
    val eventSink: (KnockRequestsListEvent) -> Unit,
) {
    val canAcceptAll = permissions.canAccept && knockRequests is AsyncData.Success && knockRequests.data.size > 1
}

@Immutable
sealed interface KnockRequestsAction {
    data object None : KnockRequestsAction
    data class Accept(val knockRequest: KnockRequestPresentable) : KnockRequestsAction
    data class Decline(val knockRequest: KnockRequestPresentable) : KnockRequestsAction
    data class DeclineAndBan(val knockRequest: KnockRequestPresentable) : KnockRequestsAction
    data object AcceptAll : KnockRequestsAction
}
