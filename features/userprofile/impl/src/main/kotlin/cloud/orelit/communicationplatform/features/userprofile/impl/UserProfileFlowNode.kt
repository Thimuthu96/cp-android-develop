/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.userprofile.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.features.call.api.CallData
import cloud.orelit.communicationplatform.features.call.api.ElementCallEntryPoint
import cloud.orelit.communicationplatform.features.userprofile.api.UserProfileEntryPoint
import cloud.orelit.communicationplatform.features.userprofile.impl.root.UserProfileNode
import cloud.orelit.communicationplatform.features.userprofile.shared.UserProfileNodeHelper
import cloud.orelit.communicationplatform.features.verifysession.api.OutgoingVerificationEntryPoint
import cloud.orelit.communicationplatform.libraries.architecture.BackstackView
import cloud.orelit.communicationplatform.libraries.architecture.BaseFlowNode
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import cloud.orelit.communicationplatform.libraries.architecture.inputs
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.notification.CallIntent
import cloud.orelit.communicationplatform.libraries.matrix.api.verification.VerificationRequest
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.MediaViewerEntryPoint
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
@AssistedInject
class UserProfileFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val elementCallEntryPoint: ElementCallEntryPoint,
    private val sessionId: SessionId,
    private val mediaViewerEntryPoint: MediaViewerEntryPoint,
    private val outgoingVerificationEntryPoint: OutgoingVerificationEntryPoint,
) : BaseFlowNode<UserProfileFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data class AvatarPreview(val name: String, val avatarUrl: String) : NavTarget

        @Parcelize
        data class VerifyUser(val userId: UserId) : NavTarget
    }

    private val callback: UserProfileEntryPoint.Callback = callback()
    private val inputs = inputs<UserProfileEntryPoint.Params>()

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Root -> {
                val callback = object : UserProfileNodeHelper.Callback {
                    override fun navigateToAvatarPreview(username: String, avatarUrl: String) {
                        backstack.push(NavTarget.AvatarPreview(username, avatarUrl))
                    }

                    override fun navigateToRoom(roomId: RoomId) {
                        callback.navigateToRoom(roomId)
                    }

                    override fun startCall(dmRoomId: RoomId, callIntent: CallIntent) {
                        elementCallEntryPoint.startCall(
                            CallData(
                                sessionId = sessionId,
                                roomId = dmRoomId,
                                isAudioCall = callIntent == CallIntent.AUDIO
                            )
                        )
                    }

                    override fun startVerifyUserFlow(userId: UserId) {
                        backstack.push(NavTarget.VerifyUser(userId))
                    }
                }
                val params = UserProfileNode.UserProfileInputs(userId = inputs.userId)
                createNode<UserProfileNode>(buildContext, listOf(callback, params))
            }
            is NavTarget.AvatarPreview -> {
                val callback = object : MediaViewerEntryPoint.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }

                    override fun viewInTimeline(eventId: EventId) {
                        // Cannot happen
                    }

                    override fun forwardEvent(eventId: EventId, fromPinnedEvents: Boolean) {
                        // Cannot happen
                    }
                }
                val params = mediaViewerEntryPoint.createParamsForAvatar(
                    filename = navTarget.name,
                    avatarUrl = navTarget.avatarUrl,
                )
                mediaViewerEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = params,
                    callback = callback,
                )
            }
            is NavTarget.VerifyUser -> {
                val params = OutgoingVerificationEntryPoint.Params(
                    showDeviceVerifiedScreen = false,
                    verificationRequest = VerificationRequest.Outgoing.User(userId = navTarget.userId)
                )
                outgoingVerificationEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = params,
                    callback = object : OutgoingVerificationEntryPoint.Callback {
                        override fun navigateToLearnMoreAboutEncryption() {
                            // No op
                        }

                        override fun onBack() {
                            // No op
                        }

                        override fun onDone() {
                            // No op
                        }
                    }
                )
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}
