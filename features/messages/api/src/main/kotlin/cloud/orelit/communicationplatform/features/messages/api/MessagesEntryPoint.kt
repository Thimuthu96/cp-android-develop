/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.api

import android.os.Parcelable
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import cloud.orelit.communicationplatform.libraries.architecture.FeatureEntryPoint
import cloud.orelit.communicationplatform.libraries.architecture.NodeInputs
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.ThreadId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkData
import kotlinx.parcelize.Parcelize

interface MessagesEntryPoint : FeatureEntryPoint {
    sealed interface InitialTarget : Parcelable {
        @Parcelize
        data class Messages(
            val focusedEventId: EventId?,
        ) : InitialTarget

        @Parcelize
        data object PinnedMessages : InitialTarget
    }

    interface Callback : Plugin {
        fun navigateToRoomDetails()
        fun navigateToRoomMemberDetails(userId: UserId)
        fun handlePermalinkClick(data: PermalinkData, pushToBackstack: Boolean)
        fun forwardEvent(eventId: EventId, fromPinnedEvents: Boolean)
        fun navigateToRoom(roomId: RoomId)
        fun navigateToDeveloperSettings()
    }

    data class Params(val initialTarget: InitialTarget) : NodeInputs

    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: Params,
        callback: Callback,
    ): Node

    /**
     * Lets callers outside this feature drive an already created messages node, for navigation that has to happen after it is attached.
     */
    interface NodeProxy {
        /**
         * Opens a thread on top of the messages screen, suspending until the thread node is attached.
         *
         * @param threadId the thread to open.
         * @param focusedEventId the event to scroll to within the thread, or `null` to open at the latest message.
         */
        suspend fun attachThread(threadId: ThreadId, focusedEventId: EventId?)
    }
}
