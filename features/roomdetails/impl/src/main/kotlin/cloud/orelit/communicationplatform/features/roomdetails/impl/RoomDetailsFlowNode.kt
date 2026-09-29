/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.roomdetails.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.Interaction
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.appconfig.LearnMoreConfig
import cloud.orelit.communicationplatform.features.call.api.CallData
import cloud.orelit.communicationplatform.features.call.api.ElementCallEntryPoint
import cloud.orelit.communicationplatform.features.knockrequests.api.list.KnockRequestsListEntryPoint
import cloud.orelit.communicationplatform.features.messages.api.MessagesEntryPoint
import cloud.orelit.communicationplatform.features.poll.api.history.PollHistoryEntryPoint
import cloud.orelit.communicationplatform.features.reportroom.api.ReportRoomEntryPoint
import cloud.orelit.communicationplatform.features.rolesandpermissions.api.ChangeRoomMemberRolesEntryPoint
import cloud.orelit.communicationplatform.features.rolesandpermissions.api.ChangeRoomMemberRolesListType
import cloud.orelit.communicationplatform.features.rolesandpermissions.api.RolesAndPermissionsEntryPoint
import cloud.orelit.communicationplatform.features.roomdetails.api.RoomDetailsEntryPoint
import cloud.orelit.communicationplatform.features.roomdetails.impl.invite.RoomInviteMembersNode
import cloud.orelit.communicationplatform.features.roomdetails.impl.members.RoomMemberListNode
import cloud.orelit.communicationplatform.features.roomdetails.impl.members.details.RoomMemberDetailsNode
import cloud.orelit.communicationplatform.features.roomdetails.impl.notificationsettings.RoomNotificationSettingsNode
import cloud.orelit.communicationplatform.features.roomdetailsedit.api.RoomDetailsEditEntryPoint
import cloud.orelit.communicationplatform.features.securityandprivacy.api.SecurityAndPrivacyEntryPoint
import cloud.orelit.communicationplatform.features.userprofile.shared.UserProfileNodeHelper
import cloud.orelit.communicationplatform.features.verifysession.api.OutgoingVerificationEntryPoint
import cloud.orelit.communicationplatform.libraries.architecture.BackstackWithOverlayBox
import cloud.orelit.communicationplatform.libraries.architecture.BaseFlowNode
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import cloud.orelit.communicationplatform.libraries.architecture.overlay.operation.hide
import cloud.orelit.communicationplatform.libraries.architecture.overlay.operation.show
import cloud.orelit.communicationplatform.libraries.designsystem.utils.OpenUrlInTabView
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.toRoomIdOrAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.notification.CallIntent
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkData
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.verification.VerificationRequest
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.MediaGalleryEntryPoint
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.MediaViewerEntryPoint
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService
import cloud.orelit.communicationplatform.services.analyticsproviders.api.trackers.captureInteraction
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
@AssistedInject
class RoomDetailsFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val pollHistoryEntryPoint: PollHistoryEntryPoint,
    private val elementCallEntryPoint: ElementCallEntryPoint,
    private val room: JoinedRoom,
    private val analyticsService: AnalyticsService,
    private val messagesEntryPoint: MessagesEntryPoint,
    private val knockRequestsListEntryPoint: KnockRequestsListEntryPoint,
    private val mediaViewerEntryPoint: MediaViewerEntryPoint,
    private val mediaGalleryEntryPoint: MediaGalleryEntryPoint,
    private val outgoingVerificationEntryPoint: OutgoingVerificationEntryPoint,
    private val reportRoomEntryPoint: ReportRoomEntryPoint,
    private val changeRoomMemberRolesEntryPoint: ChangeRoomMemberRolesEntryPoint,
    private val rolesAndPermissionsEntryPoint: RolesAndPermissionsEntryPoint,
    private val securityAndPrivacyEntryPoint: SecurityAndPrivacyEntryPoint,
    private val roomDetailsEditEntryPoint: RoomDetailsEditEntryPoint,
) : BaseFlowNode<RoomDetailsFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = plugins.filterIsInstance<RoomDetailsEntryPoint.Params>().first().initialElement.toNavTarget(),
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object RoomDetails : NavTarget

        @Parcelize
        data object RoomMemberList : NavTarget

        @Parcelize
        data object RoomDetailsEdit : NavTarget

        @Parcelize
        data object InviteMembers : NavTarget

        @Parcelize
        data class RoomNotificationSettings(
            /**
             * When presented from outside the context of the room, the rooms settings UI is different.
             * Figma designs: https://www.figma.com/file/0MMNu7cTOzLOlWb7ctTkv3/Element-X?type=design&node-id=5199-198932&mode=design&t=fTTvpuxYFjewYQOe-0
             */
            val showUserDefinedSettingStyle: Boolean
        ) : NavTarget

        @Parcelize
        data class RoomMemberDetails(val roomMemberId: UserId) : NavTarget

        @Parcelize
        data class AvatarPreview(val name: String, val avatarUrl: String) : NavTarget

        @Parcelize
        data object PollHistory : NavTarget

        @Parcelize
        data object MediaGallery : NavTarget

        @Parcelize
        data object AdminSettings : NavTarget

        @Parcelize
        data object PinnedMessagesList : NavTarget

        @Parcelize
        data object KnockRequestsList : NavTarget

        @Parcelize
        data object SecurityAndPrivacy : NavTarget

        @Parcelize
        data class VerifyUser(val userId: UserId) : NavTarget

        @Parcelize
        data object ReportRoom : NavTarget

        @Parcelize
        data object SelectNewOwnersWhenLeaving : NavTarget
    }

    private val callback: RoomDetailsEntryPoint.Callback = callback()

    override fun onBuilt() {
        super.onBuilt()
        whenChildrenAttached {
            commonLifecycle: Lifecycle,
            roomDetailsNode: RoomDetailsNode,
            changeRoomMemberRolesNode: ChangeRoomMemberRolesEntryPoint.NodeProxy,
            ->
            commonLifecycle.coroutineScope.launch {
                val isNewOwnerSelected = changeRoomMemberRolesNode.waitForCompletion()
                withContext(NonCancellable) {
                    backstack.pop()
                    if (isNewOwnerSelected) {
                        roomDetailsNode.onNewOwnersSelected()
                    }
                }
            }
        }
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.RoomDetails -> {
                val roomDetailsCallback = object : RoomDetailsNode.Callback {
                    override fun navigateBack() {
                        callback.onDone()
                    }

                    override fun navigateToRoomMemberList() {
                        backstack.push(NavTarget.RoomMemberList)
                    }

                    override fun navigateToRoomDetailsEdit() {
                        backstack.push(NavTarget.RoomDetailsEdit)
                    }

                    override fun navigateToInviteMembers() {
                        backstack.push(NavTarget.InviteMembers)
                    }

                    override fun navigateToRoomNotificationSettings() {
                        backstack.push(NavTarget.RoomNotificationSettings(showUserDefinedSettingStyle = false))
                    }

                    override fun navigateToAvatarPreview(name: String, url: String) {
                        overlay.show(NavTarget.AvatarPreview(name, url))
                    }

                    override fun navigateToPollHistory() {
                        backstack.push(NavTarget.PollHistory)
                    }

                    override fun navigateToMediaGallery() {
                        backstack.push(NavTarget.MediaGallery)
                    }

                    override fun navigateToAdminSettings() {
                        backstack.push(NavTarget.AdminSettings)
                    }

                    override fun navigateToPinnedMessagesList() {
                        backstack.push(NavTarget.PinnedMessagesList)
                    }

                    override fun navigateToKnockRequestsList() {
                        backstack.push(NavTarget.KnockRequestsList)
                    }

                    override fun navigateToSecurityAndPrivacy() {
                        backstack.push(NavTarget.SecurityAndPrivacy)
                    }

                    override fun navigateToRoomMemberDetails(userId: UserId) {
                        backstack.push(NavTarget.RoomMemberDetails(userId))
                    }

                    override fun navigateToRoomCall(callIntent: CallIntent) {
                        val callData = CallData(
                            sessionId = room.sessionId,
                            roomId = room.roomId,
                            isAudioCall = callIntent == CallIntent.AUDIO
                        )
                        analyticsService.captureInteraction(Interaction.Name.MobileRoomCallButton)
                        elementCallEntryPoint.startCall(callData)
                    }

                    override fun navigateToReportRoom() {
                        backstack.push(NavTarget.ReportRoom)
                    }

                    override fun navigateToSelectNewOwnersWhenLeaving() {
                        backstack.push(NavTarget.SelectNewOwnersWhenLeaving)
                    }
                }
                createNode<RoomDetailsNode>(buildContext, listOf(roomDetailsCallback))
            }

            NavTarget.RoomMemberList -> {
                val roomMemberListCallback = object : RoomMemberListNode.Callback {
                    override fun navigateToRoomMemberDetails(roomMemberId: UserId) {
                        backstack.push(NavTarget.RoomMemberDetails(roomMemberId))
                    }

                    override fun navigateToInviteMembers() {
                        backstack.push(NavTarget.InviteMembers)
                    }

                    override fun navigateToAvatarPreview(username: String, avatarUrl: String) {
                        overlay.show(NavTarget.AvatarPreview(username, avatarUrl))
                    }
                }
                createNode<RoomMemberListNode>(buildContext, listOf(roomMemberListCallback))
            }

            NavTarget.RoomDetailsEdit -> {
                roomDetailsEditEntryPoint.createNode(this, buildContext)
            }

            NavTarget.InviteMembers -> {
                val callback = object : RoomInviteMembersNode.Callback {
                    override fun openCreatedRoom(roomId: RoomId) {
                        navigateUp()
                        room.roomCoroutineScope.launch {
                            callback.navigateToRoom(
                                roomId = roomId,
                                serverNames = emptyList(),
                                // Remove the invite screen from the backstack to avoid navigating back to it after the new room has been created
                                clearBackStack = true,
                            )
                        }
                    }
                }
                createNode<RoomInviteMembersNode>(buildContext, plugins = listOf(callback))
            }

            is NavTarget.RoomNotificationSettings -> {
                val input = RoomNotificationSettingsNode.RoomNotificationSettingInput(navTarget.showUserDefinedSettingStyle)
                val callback = object : RoomNotificationSettingsNode.Callback {
                    override fun navigateToGlobalNotificationSettings() {
                        callback.navigateToGlobalNotificationSettings()
                    }
                }
                createNode<RoomNotificationSettingsNode>(buildContext, listOf(input, callback))
            }

            is NavTarget.RoomMemberDetails -> {
                val callback = object : UserProfileNodeHelper.Callback {
                    override fun navigateToAvatarPreview(username: String, avatarUrl: String) {
                        overlay.show(NavTarget.AvatarPreview(username, avatarUrl))
                    }

                    override fun navigateToRoom(roomId: RoomId) {
                        callback.navigateToRoom(roomId, emptyList())
                    }

                    override fun startCall(dmRoomId: RoomId, callIntent: CallIntent) {
                        elementCallEntryPoint.startCall(
                            CallData(
                                roomId = dmRoomId,
                                sessionId = room.sessionId,
                                isAudioCall = callIntent == CallIntent.AUDIO
                            )
                        )
                    }

                    override fun startVerifyUserFlow(userId: UserId) {
                        backstack.push(NavTarget.VerifyUser(userId))
                    }
                }
                val plugins = listOf(RoomMemberDetailsNode.RoomMemberDetailsInput(navTarget.roomMemberId), callback)
                createNode<RoomMemberDetailsNode>(buildContext, plugins)
            }
            is NavTarget.AvatarPreview -> {
                val callback = object : MediaViewerEntryPoint.Callback {
                    override fun onDone() {
                        overlay.hide()
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
            is NavTarget.PollHistory -> {
                pollHistoryEntryPoint.createNode(this, buildContext)
            }
            is NavTarget.MediaGallery -> {
                val callback = object : MediaGalleryEntryPoint.Callback {
                    override fun onBackClick() {
                        backstack.pop()
                    }

                    override fun viewInTimeline(eventId: EventId) {
                        val permalinkData = PermalinkData.RoomLink(
                            roomIdOrAlias = room.roomId.toRoomIdOrAlias(),
                            eventId = eventId,
                        )
                        callback.handlePermalinkClick(permalinkData, pushToBackstack = false)
                    }

                    override fun forward(eventId: EventId, fromPinnedEvents: Boolean) {
                        callback.startForwardEventFlow(eventId, fromPinnedEvents)
                    }
                }
                mediaGalleryEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = callback,
                )
            }

            is NavTarget.AdminSettings -> {
                val callback = object : RolesAndPermissionsEntryPoint.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }
                }
                rolesAndPermissionsEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = callback,
                )
            }
            NavTarget.PinnedMessagesList -> {
                val params = MessagesEntryPoint.Params(
                    MessagesEntryPoint.InitialTarget.PinnedMessages
                )
                val callback = object : MessagesEntryPoint.Callback {
                    override fun navigateToRoomDetails() = Unit

                    override fun navigateToRoomMemberDetails(userId: UserId) = Unit

                    override fun handlePermalinkClick(data: PermalinkData, pushToBackstack: Boolean) {
                        callback.handlePermalinkClick(data, pushToBackstack)
                    }

                    override fun forwardEvent(eventId: EventId, fromPinnedEvents: Boolean) {
                        callback.startForwardEventFlow(eventId, fromPinnedEvents)
                    }

                    override fun navigateToRoom(roomId: RoomId) {
                        callback.navigateToRoom(roomId, emptyList())
                    }

                    override fun navigateToDeveloperSettings() {
                        callback.navigateToDeveloperSettings()
                    }
                }
                return messagesEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = params,
                    callback = callback,
                )
            }
            NavTarget.KnockRequestsList -> {
                knockRequestsListEntryPoint.createNode(this, buildContext)
            }
            NavTarget.SecurityAndPrivacy -> {
                val callback = object : SecurityAndPrivacyEntryPoint.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }
                }
                securityAndPrivacyEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = callback,
                )
            }
            is NavTarget.VerifyUser -> {
                val params = OutgoingVerificationEntryPoint.Params(
                    showDeviceVerifiedScreen = true,
                    verificationRequest = VerificationRequest.Outgoing.User(userId = navTarget.userId)
                )
                outgoingVerificationEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = params,
                    callback = object : OutgoingVerificationEntryPoint.Callback {
                        override fun onDone() {
                            backstack.pop()
                        }

                        override fun onBack() {
                            backstack.pop()
                        }

                        override fun navigateToLearnMoreAboutEncryption() {
                            learnMoreUrl.value = LearnMoreConfig.ENCRYPTION_URL
                        }
                    },
                )
            }
            is NavTarget.ReportRoom -> {
                reportRoomEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    roomId = room.roomId,
                )
            }

            is NavTarget.SelectNewOwnersWhenLeaving -> {
                changeRoomMemberRolesEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    room = room,
                    listType = ChangeRoomMemberRolesListType.SelectNewOwnersWhenLeaving,
                )
            }
        }
    }

    private val learnMoreUrl = mutableStateOf<String?>(null)

    @Composable
    override fun View(modifier: Modifier) {
        BackstackWithOverlayBox(modifier)

        OpenUrlInTabView(learnMoreUrl)
    }
}
