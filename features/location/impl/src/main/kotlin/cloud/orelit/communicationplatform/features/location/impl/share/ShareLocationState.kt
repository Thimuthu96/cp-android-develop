/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.location.impl.share

import cloud.orelit.communicationplatform.features.enterprise.api.remoteconfig.MapTilerConfig
import cloud.orelit.communicationplatform.features.location.impl.common.ui.LocationConstraintsDialogState
import cloud.orelit.communicationplatform.features.location.impl.common.userlocation.UserLocationState
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList

data class ShareLocationState(
    val customMapTilerConfig: AsyncData<MapTilerConfig?>,
    val currentUser: MatrixUser,
    val dialogState: Dialog,
    val trackUserLocation: Boolean,
    val userLocationState: UserLocationState,
    val appName: String,
    val canShareLiveLocation: Boolean,
    val startLiveLocationAction: AsyncAction<Unit>,
    val eventSink: (ShareLocationEvent) -> Unit,
) {
    sealed interface Dialog {
        data object None : Dialog
        data class Constraints(val state: LocationConstraintsDialogState) : Dialog
        data object LiveLocationDisclaimer : Dialog
        data class LiveLocationDurations(val durations: ImmutableList<LiveLocationDuration>) : Dialog
    }
}
