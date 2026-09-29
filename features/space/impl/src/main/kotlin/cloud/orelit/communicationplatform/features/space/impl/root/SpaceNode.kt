/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.space.impl.root

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.features.invite.api.acceptdecline.AcceptDeclineInviteView
import cloud.orelit.communicationplatform.features.space.impl.di.SpaceFlowScope
import cloud.orelit.communicationplatform.libraries.androidutils.R
import cloud.orelit.communicationplatform.libraries.androidutils.system.startSharePlainTextIntent
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.spaces.SpaceRoomList
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.launch
import timber.log.Timber

@ContributesNode(SpaceFlowScope::class)
@AssistedInject
class SpaceNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: SpacePresenter,
    private val matrixClient: MatrixClient,
    private val spaceRoomList: SpaceRoomList,
    private val acceptDeclineInviteView: AcceptDeclineInviteView,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun navigateToRoom(roomId: RoomId, viaParameters: List<String>)
        fun navigateToSpaceSettings()
        fun navigateToRoomMemberList()
        fun startLeaveSpaceFlow()
        fun navigateToAddRoom()

        fun onCreateRoom()
    }

    private val callback: Callback = callback()

    private fun onShareRoom(context: Context) = lifecycleScope.launch {
        matrixClient.getRoom(spaceRoomList.spaceId)?.use { room ->
            room.getPermalink()
                .onSuccess { permalink ->
                    context.startSharePlainTextIntent(
                        activityResultLauncher = null,
                        chooserTitle = context.getString(CommonStrings.common_share_space),
                        text = permalink,
                        noActivityFoundMessage = context.getString(R.string.error_no_compatible_app_found)
                    )
                }
                .onFailure {
                    Timber.e(it)
                }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val context = LocalContext.current
        SpaceView(
            state = state,
            onBackClick = ::navigateUp,
            onLeaveSpaceClick = {
                callback.startLeaveSpaceFlow()
            },
            onRoomClick = { spaceRoom ->
                callback.navigateToRoom(spaceRoom.roomId, spaceRoom.via)
            },
            onSettingsClick = {
                callback.navigateToSpaceSettings()
            },
            onShareSpace = {
                onShareRoom(context)
            },
            onViewMembersClick = {
                callback.navigateToRoomMemberList()
            },
            onAddRoomClick = {
                callback.navigateToAddRoom()
            },
            acceptDeclineInviteView = {
                acceptDeclineInviteView.Render(
                    state = state.acceptDeclineInviteState,
                    onAcceptInviteSuccess = { roomId ->
                        callback.navigateToRoom(roomId, emptyList())
                    },
                    onDeclineInviteSuccess = { roomId ->
                        // No action needed
                    },
                    modifier = Modifier
                )
            },
            onCreateRoomClick = callback::onCreateRoom,
            modifier = modifier
        )
    }
}
