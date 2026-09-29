/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomdetails.api

import android.os.Parcelable
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import cloud.orelit.communicationplatform.libraries.architecture.FeatureEntryPoint
import cloud.orelit.communicationplatform.libraries.architecture.NodeInputs
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkData
import kotlinx.parcelize.Parcelize

interface RoomDetailsEntryPoint : FeatureEntryPoint {
    sealed interface InitialTarget : Parcelable {
        @Parcelize
        data object RoomDetails : InitialTarget

        @Parcelize
        data object RoomMemberList : InitialTarget

        @Parcelize
        data class RoomMemberDetails(val roomMemberId: UserId) : InitialTarget

        @Parcelize
        data object RoomNotificationSettings : InitialTarget
    }

    data class Params(val initialElement: InitialTarget) : NodeInputs

    interface Callback : Plugin {
        fun onDone()
        fun navigateToGlobalNotificationSettings()
        fun navigateToDeveloperSettings()
        fun navigateToRoom(roomId: RoomId, serverNames: List<String>, clearBackStack: Boolean = false)
        fun handlePermalinkClick(data: PermalinkData, pushToBackstack: Boolean)
        fun startForwardEventFlow(eventId: EventId, fromPinnedEvents: Boolean)
    }

    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: Params,
        callback: Callback,
    ): Node
}
