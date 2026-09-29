/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.forward.impl

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.model.permanent.PermanentNavModel
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.features.forward.api.ForwardEntryPoint
import cloud.orelit.communicationplatform.libraries.architecture.NodeInputs
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.architecture.inputs
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.TimelineProvider
import cloud.orelit.communicationplatform.libraries.roomselect.api.RoomSelectEntryPoint
import cloud.orelit.communicationplatform.libraries.roomselect.api.RoomSelectMode
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
@AssistedInject
class ForwardMessagesNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: ForwardMessagesPresenter.Factory,
    private val roomSelectEntryPoint: RoomSelectEntryPoint,
) : ParentNode<ForwardMessagesNode.NavTarget>(
    navModel = PermanentNavModel(
        navTargets = setOf(NavTarget),
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    @Parcelize
    object NavTarget : Parcelable

    data class Inputs(
        val eventId: EventId,
        val timelineProvider: TimelineProvider,
    ) : NodeInputs

    private val inputs = inputs<Inputs>()
    private val callback: ForwardEntryPoint.Callback = callback()
    private val presenter = presenterFactory.create(inputs.eventId.value, inputs.timelineProvider)

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        val callback = object : RoomSelectEntryPoint.Callback {
            override fun onRoomSelected(roomIds: List<RoomId>) {
                presenter.onRoomSelected(roomIds)
            }

            override fun onCancel() {
                callback.onDone(emptyList())
            }
        }

        return roomSelectEntryPoint.createNode(
            parentNode = this,
            buildContext = buildContext,
            params = RoomSelectEntryPoint.Params(
                mode = RoomSelectMode.Forward,
                maxNumberOfRooms = RoomSelectEntryPoint.DEFAULT_MAX_NUMBER_OF_ROOMS,
            ),
            callback = callback,
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        Box(modifier = modifier) {
            // Will render to room select screen
            Children(
                navModel = navModel,
            )

            val state = presenter.present()
            ForwardMessagesView(
                state = state,
                onForwardSuccess = callback::onDone,
            )
        }
    }
}
