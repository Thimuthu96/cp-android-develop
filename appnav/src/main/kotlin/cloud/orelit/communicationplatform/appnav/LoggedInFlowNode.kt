/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.appnav

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.composable.PermanentChild
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.NavElements
import com.bumble.appyx.core.navigation.NavKey
import com.bumble.appyx.core.navigation.model.permanent.PermanentNavModel
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.BackStack.State.ACTIVE
import com.bumble.appyx.navmodel.backstack.BackStack.State.CREATED
import com.bumble.appyx.navmodel.backstack.BackStack.State.STASHED
import com.bumble.appyx.navmodel.backstack.BackStackElement
import com.bumble.appyx.navmodel.backstack.BackStackElements
import com.bumble.appyx.navmodel.backstack.operation.BackStackOperation
import com.bumble.appyx.navmodel.backstack.operation.Push
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.replace
import com.bumble.appyx.navmodel.backstack.operation.singleTop
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.annotations.ContributesNode
import cloud.orelit.communicationplatform.appnav.loggedin.LoggedInNode
import cloud.orelit.communicationplatform.appnav.loggedin.MediaPreviewConfigMigration
import cloud.orelit.communicationplatform.appnav.loggedin.SendQueues
import cloud.orelit.communicationplatform.appnav.room.RoomFlowNode
import cloud.orelit.communicationplatform.appnav.room.RoomNavigationTarget
import cloud.orelit.communicationplatform.appnav.room.joined.JoinedRoomLoadedFlowNode
import cloud.orelit.communicationplatform.compound.colors.SemanticColorsLightDark
import cloud.orelit.communicationplatform.features.createroom.api.CreateRoomEntryPoint
import cloud.orelit.communicationplatform.features.enterprise.api.EnterpriseService
import cloud.orelit.communicationplatform.features.enterprise.api.SessionEnterpriseService
import cloud.orelit.communicationplatform.features.enterprise.api.remoteconfig.CustomMapTilerConfigProvider
import cloud.orelit.communicationplatform.features.ftue.api.FtueEntryPoint
import cloud.orelit.communicationplatform.features.ftue.api.state.FtueService
import cloud.orelit.communicationplatform.features.ftue.api.state.FtueState
import cloud.orelit.communicationplatform.features.home.api.HomeEntryPoint
import cloud.orelit.communicationplatform.features.linknewdevice.api.LinkNewDeviceEntryPoint
import cloud.orelit.communicationplatform.features.location.api.LocalMapTilerConfig
import cloud.orelit.communicationplatform.features.location.api.live.ActiveLiveLocationShareManager
import cloud.orelit.communicationplatform.features.networkmonitor.api.NetworkMonitor
import cloud.orelit.communicationplatform.features.networkmonitor.api.NetworkStatus
import cloud.orelit.communicationplatform.features.networkmonitor.api.ui.ConnectivityIndicatorContainer
import cloud.orelit.communicationplatform.features.preferences.api.PreferencesEntryPoint
import cloud.orelit.communicationplatform.features.roomdirectory.api.RoomDescription
import cloud.orelit.communicationplatform.features.roomdirectory.api.RoomDirectoryEntryPoint
import cloud.orelit.communicationplatform.features.securebackup.api.SecureBackupEntryPoint
import cloud.orelit.communicationplatform.features.share.api.ShareEntryPoint
import cloud.orelit.communicationplatform.features.share.api.ShareIntentData
import cloud.orelit.communicationplatform.features.startchat.api.StartChatEntryPoint
import cloud.orelit.communicationplatform.features.userprofile.api.UserProfileEntryPoint
import cloud.orelit.communicationplatform.features.verifysession.api.IncomingVerificationEntryPoint
import cloud.orelit.communicationplatform.libraries.architecture.BackstackView
import cloud.orelit.communicationplatform.libraries.architecture.BaseFlowNode
import cloud.orelit.communicationplatform.libraries.architecture.callback
import cloud.orelit.communicationplatform.libraries.architecture.createNode
import cloud.orelit.communicationplatform.libraries.architecture.waitForChildAttached
import cloud.orelit.communicationplatform.libraries.architecture.waitForNavTargetAttached
import cloud.orelit.communicationplatform.libraries.core.meta.BuildMeta
import cloud.orelit.communicationplatform.libraries.designsystem.theme.ElementThemeApp
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.di.annotations.SessionCoroutineScope
import cloud.orelit.communicationplatform.libraries.featureflag.api.FeatureFlagService
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomIdOrAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.toRoomIdOrAlias
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkData
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.sync.SyncService
import cloud.orelit.communicationplatform.libraries.matrix.api.verification.SessionVerificationServiceListener
import cloud.orelit.communicationplatform.libraries.matrix.api.verification.VerificationRequest
import cloud.orelit.communicationplatform.libraries.preferences.api.store.AppPreferencesStore
import cloud.orelit.communicationplatform.libraries.push.api.notifications.conversations.NotificationConversationService
import cloud.orelit.communicationplatform.libraries.ui.common.nodes.emptyNode
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsLongRunningTransaction
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService
import cloud.orelit.communicationplatform.services.analytics.api.watchers.AnalyticsRoomListStateWatcher
import cloud.orelit.communicationplatform.services.appnavstate.api.AppNavigationStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import java.util.Optional
import java.util.UUID
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration
import im.vector.app.features.analytics.plan.JoinedRoom as JoinedRoomAnalyticsEvent

// The maximum number of room nodes that should be kept in the backstack at the same time.
// Having 5 rooms in the backstack seems reasonable and shouldn't grow the saved state size too much.
private const val MAX_ROOM_NODE_COUNT = 5

@ContributesNode(SessionScope::class)
@AssistedInject
class LoggedInFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val homeEntryPoint: HomeEntryPoint,
    private val preferencesEntryPoint: PreferencesEntryPoint,
    private val startChatEntryPoint: StartChatEntryPoint,
    private val appNavigationStateService: AppNavigationStateService,
    private val secureBackupEntryPoint: SecureBackupEntryPoint,
    private val userProfileEntryPoint: UserProfileEntryPoint,
    private val ftueEntryPoint: FtueEntryPoint,
    private val linkNewDeviceEntryPoint: LinkNewDeviceEntryPoint,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val ftueService: FtueService,
    private val roomDirectoryEntryPoint: RoomDirectoryEntryPoint,
    private val shareEntryPoint: ShareEntryPoint,
    private val matrixClient: MatrixClient,
    private val sendingQueue: SendQueues,
    private val incomingVerificationEntryPoint: IncomingVerificationEntryPoint,
    private val mediaPreviewConfigMigration: MediaPreviewConfigMigration,
    private val sessionEnterpriseService: SessionEnterpriseService,
    private val networkMonitor: NetworkMonitor,
    private val notificationConversationService: NotificationConversationService,
    private val syncService: SyncService,
    private val enterpriseService: EnterpriseService,
    private val appPreferencesStore: AppPreferencesStore,
    private val featureFlagService: FeatureFlagService,
    private val buildMeta: BuildMeta,
    snackbarDispatcher: SnackbarDispatcher,
    private val analyticsService: AnalyticsService,
    private val analyticsRoomListStateWatcher: AnalyticsRoomListStateWatcher,
    private val createRoomEntryPoint: CreateRoomEntryPoint,
    private val activeLiveLocationShareManager: ActiveLiveLocationShareManager,
    private val customMapTilerConfigProvider: CustomMapTilerConfigProvider,
) : BaseFlowNode<LoggedInFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Placeholder,
        savedStateMap = buildContext.savedStateMap,
    ),
    permanentNavModel = PermanentNavModel(
        navTargets = setOf(NavTarget.LoggedInPermanent),
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    interface Callback : Plugin {
        fun navigateToBugReport()
        fun navigateToAddAccount()
    }

    private val callback: Callback = callback()
    private val loggedInFlowProcessor = LoggedInEventProcessor(
        snackbarDispatcher = snackbarDispatcher,
        roomMembershipObserver = matrixClient.roomMembershipObserver,
    )

    private val verificationListener = object : SessionVerificationServiceListener {
        override fun onIncomingSessionRequest(verificationRequest: VerificationRequest.Incoming) {
            // Without this launch the rendering and actual state of this Appyx node's children gets out of sync, resulting in a crash.
            // This might be because this method is called back from Rust in a background thread.
            lifecycleScope.launch {
                val receivedAt = Instant.now()

                // Wait until the app is in foreground to display the incoming verification request
                appNavigationStateService.appNavigationState.first { it.isInForeground }

                // TODO there should also be a timeout for > 10 minutes elapsed since the request was created, but the SDK doesn't expose that info yet
                val now = Instant.now()
                val elapsedTimeSinceReceived = Duration.between(receivedAt, now).toKotlinDuration()

                // Discard the incoming verification request if it has timed out
                if (elapsedTimeSinceReceived > 2.minutes) {
                    Timber.w("Incoming verification request ${verificationRequest.details.flowId} discarded due to timeout.")
                    return@launch
                }

                // Wait for the RoomList UI to be ready so the incoming verification screen can be displayed on top of it
                // Otherwise, the RoomList UI may be incorrectly displayed on top
                withTimeout(5.seconds) {
                    backstack.elements.first { elements ->
                        elements.any { it.key.navTarget == NavTarget.Home }
                    }
                }

                backstack.singleTop(NavTarget.IncomingVerificationRequest(verificationRequest))
            }
        }
    }

    override fun onBuilt() {
        super.onBuilt()
        lifecycleScope.launch {
            sessionEnterpriseService.init()
            activeLiveLocationShareManager.setup()
        }
        lifecycle.subscribe(
            onCreate = {
                analyticsRoomListStateWatcher.start()
                appNavigationStateService.onNavigateToSession(id, matrixClient.sessionId)
                loggedInFlowProcessor.observeEvents(sessionCoroutineScope)
                matrixClient.sessionVerificationService.setListener(verificationListener)
                mediaPreviewConfigMigration()
                sessionCoroutineScope.launch {
                    // Wait for the network to be connected before pre-fetching the max file upload size
                    networkMonitor.connectivity.first { networkStatus -> networkStatus == NetworkStatus.Connected }
                    matrixClient.getMaxFileUploadSize()
                }

                analyticsService.startLongRunningTransaction(AnalyticsLongRunningTransaction.FirstRoomsDisplayed)

                ftueService.state
                    .onEach { ftueState ->
                        when (ftueState) {
                            is FtueState.Unknown -> Unit // Nothing to do
                            is FtueState.Incomplete -> backstack.safeRoot(NavTarget.Ftue)
                            is FtueState.Complete -> backstack.safeRoot(NavTarget.Home)
                        }
                    }
                    .launchIn(lifecycleScope)
            },
            onResume = {
                lifecycleScope.launch {
                    val availableRoomIds = matrixClient.getJoinedRoomIds().getOrNull() ?: return@launch
                    notificationConversationService.onAvailableRoomsChanged(sessionId = matrixClient.sessionId, roomIds = availableRoomIds)
                }
            },
            onDestroy = {
                appNavigationStateService.onLeavingSession(id)
                loggedInFlowProcessor.stopObserving()
                matrixClient.sessionVerificationService.setListener(null)
                analyticsRoomListStateWatcher.stop()
            }
        )
        setupSendingQueue()
    }

    private fun setupSendingQueue() {
        sendingQueue.launchIn(lifecycleScope)
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Placeholder : NavTarget

        @Parcelize
        data object LoggedInPermanent : NavTarget

        @Parcelize
        data object Home : NavTarget

        @Parcelize
        data class Room(
            val roomIdOrAlias: RoomIdOrAlias,
            val serverNames: List<String> = emptyList(),
            val trigger: JoinedRoomAnalyticsEvent.Trigger? = null,
            val roomDescription: RoomDescription? = null,
            val initialElement: RoomNavigationTarget = RoomNavigationTarget.Root(),
            val targetId: UUID = UUID.randomUUID(),
        ) : NavTarget

        @Parcelize
        data class UserProfile(
            val userId: UserId,
        ) : NavTarget

        @Parcelize
        data class Settings(
            val initialElement: PreferencesEntryPoint.InitialTarget = PreferencesEntryPoint.InitialTarget.Root
        ) : NavTarget

        @Parcelize
        data object CreateRoom : NavTarget

        @Parcelize
        data object CreateSpace : NavTarget

        @Parcelize
        data class SecureBackup(
            val initialElement: SecureBackupEntryPoint.InitialTarget = SecureBackupEntryPoint.InitialTarget.Root
        ) : NavTarget

        @Parcelize
        data object Ftue : NavTarget

        @Parcelize
        data object LinkNewDevice : NavTarget

        @Parcelize
        data object RoomDirectory : NavTarget

        @Parcelize
        data class IncomingShare(val shareIntentData: ShareIntentData) : NavTarget

        @Parcelize
        data class IncomingVerificationRequest(val data: VerificationRequest.Incoming) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.Placeholder -> emptyNode(buildContext)
            NavTarget.LoggedInPermanent -> {
                val callback = object : LoggedInNode.Callback {
                    override fun navigateToNotificationTroubleshoot() {
                        backstack.push(NavTarget.Settings(PreferencesEntryPoint.InitialTarget.NotificationTroubleshoot))
                    }
                }
                createNode<LoggedInNode>(buildContext, listOf(callback))
            }
            NavTarget.Home -> {
                val callback = object : HomeEntryPoint.Callback {
                    override fun navigateToRoom(roomId: RoomId, eventId: EventId?, joinedRoom: JoinedRoom?) {
                        lifecycleScope.launch {
                            attachRoom(
                                roomIdOrAlias = roomId.toRoomIdOrAlias(),
                                initialElement = RoomNavigationTarget.Root(joinedRoom = joinedRoom, eventId = eventId),
                                clearBackstack = false,
                            )
                        }
                    }

                    override fun navigateToSettings() {
                        backstack.push(NavTarget.Settings())
                    }

                    override fun navigateToCreateRoom() {
                        backstack.push(NavTarget.CreateRoom)
                    }

                    override fun navigateToCreateSpace() {
                        backstack.push(NavTarget.CreateSpace)
                    }

                    override fun navigateToSetUpRecovery() {
                        backstack.push(NavTarget.SecureBackup(initialElement = SecureBackupEntryPoint.InitialTarget.Root))
                    }

                    override fun navigateToEnterRecoveryKey() {
                        backstack.push(NavTarget.SecureBackup(initialElement = SecureBackupEntryPoint.InitialTarget.EnterRecoveryKey))
                    }

                    override fun navigateToRoomSettings(roomId: RoomId) {
                        lifecycleScope.launch {
                            attachRoom(
                                roomIdOrAlias = roomId.toRoomIdOrAlias(),
                                initialElement = RoomNavigationTarget.Details,
                                clearBackstack = false
                            )
                        }
                    }

                    override fun navigateToBugReport() {
                        callback.navigateToBugReport()
                    }
                }
                homeEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = callback,
                )
            }
            is NavTarget.Room -> {
                val joinedRoomCallback = object : JoinedRoomLoadedFlowNode.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }

                    override fun navigateToRoom(roomId: RoomId, serverNames: List<String>, clearBackStack: Boolean) {
                        lifecycleScope.launch {
                            attachRoom(roomIdOrAlias = roomId.toRoomIdOrAlias(), serverNames = serverNames, clearBackstack = clearBackStack)
                        }
                    }

                    override fun handlePermalinkClick(data: PermalinkData, pushToBackstack: Boolean) {
                        when (data) {
                            is PermalinkData.UserLink -> {
                                // Should not happen (handled by MessagesNode)
                                Timber.e("User link clicked: ${data.userId}.")
                            }
                            is PermalinkData.RoomLink -> {
                                if (pushToBackstack) {
                                    lifecycleScope.launch {
                                        attachRoom(
                                            roomIdOrAlias = data.roomIdOrAlias,
                                            serverNames = data.viaParameters,
                                            trigger = JoinedRoomAnalyticsEvent.Trigger.Timeline,
                                            initialElement = RoomNavigationTarget.Root(data.eventId),
                                            clearBackstack = false
                                        )
                                    }
                                } else {
                                    backstack.replace(
                                        NavTarget.Room(
                                            roomIdOrAlias = data.roomIdOrAlias,
                                            serverNames = data.viaParameters,
                                            trigger = JoinedRoomAnalyticsEvent.Trigger.Timeline,
                                            initialElement = RoomNavigationTarget.Root(data.eventId),
                                        )
                                    )
                                }
                            }
                            is PermalinkData.FallbackLink,
                            is PermalinkData.RoomEmailInviteLink -> {
                                // Should not happen (handled by MessagesNode)
                            }
                        }
                    }

                    override fun navigateToGlobalNotificationSettings() {
                        backstack.push(NavTarget.Settings(PreferencesEntryPoint.InitialTarget.NotificationSettings))
                    }

                    override fun navigateToDeveloperSettings() {
                        backstack.push(NavTarget.Settings(PreferencesEntryPoint.InitialTarget.DeveloperSettings))
                    }
                }
                val inputs = RoomFlowNode.Inputs(
                    roomIdOrAlias = navTarget.roomIdOrAlias,
                    roomDescription = Optional.ofNullable(navTarget.roomDescription),
                    serverNames = navTarget.serverNames,
                    trigger = Optional.ofNullable(navTarget.trigger),
                    initialElement = navTarget.initialElement
                )
                createNode<RoomFlowNode>(buildContext, plugins = listOf(inputs, joinedRoomCallback))
            }
            is NavTarget.UserProfile -> {
                val callback = object : UserProfileEntryPoint.Callback {
                    override fun navigateToRoom(roomId: RoomId) {
                        lifecycleScope.launch {
                            attachRoom(roomIdOrAlias = roomId.toRoomIdOrAlias(), clearBackstack = false)
                        }
                    }
                }
                userProfileEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = UserProfileEntryPoint.Params(userId = navTarget.userId),
                    callback = callback,
                )
            }
            is NavTarget.Settings -> {
                val callback = object : PreferencesEntryPoint.Callback {
                    override fun navigateToAddAccount() {
                        callback.navigateToAddAccount()
                    }

                    override fun navigateToLinkNewDevice() {
                        backstack.push(NavTarget.LinkNewDevice)
                    }

                    override fun navigateToBugReport() {
                        callback.navigateToBugReport()
                    }

                    override fun navigateToSecureBackup() {
                        backstack.push(NavTarget.SecureBackup())
                    }

                    override fun navigateToRoomNotificationSettings(roomId: RoomId) {
                        lifecycleScope.launch {
                            attachRoom(
                                roomIdOrAlias = roomId.toRoomIdOrAlias(),
                                initialElement = RoomNavigationTarget.NotificationSettings,
                            )
                        }
                    }

                    override fun navigateToEvent(roomId: RoomId, eventId: EventId) {
                        lifecycleScope.launch {
                            attachRoom(
                                roomIdOrAlias = roomId.toRoomIdOrAlias(),
                                initialElement = RoomNavigationTarget.Root(eventId),
                                clearBackstack = false
                            )
                        }
                    }
                }
                val inputs = PreferencesEntryPoint.Params(navTarget.initialElement)
                preferencesEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = inputs,
                    callback = callback,
                )
            }
            NavTarget.CreateRoom -> {
                val callback = object : StartChatEntryPoint.Callback {
                    override fun onRoomCreated(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>) {
                        backstack.replace(NavTarget.Room(roomIdOrAlias = roomIdOrAlias, serverNames = serverNames))
                    }

                    override fun navigateToRoomDirectory() {
                        backstack.push(NavTarget.RoomDirectory)
                    }
                }

                startChatEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = callback,
                )
            }
            is NavTarget.CreateSpace -> {
                val callback = object : CreateRoomEntryPoint.Callback {
                    override fun onRoomCreated(roomId: RoomId) {
                        lifecycleScope.launch {
                            attachRoom(
                                roomIdOrAlias = roomId.toRoomIdOrAlias(),
                                serverNames = emptyList(),
                                clearBackstack = false,
                            )
                        }
                    }
                }
                createRoomEntryPoint
                    .builder(parentNode = this, buildContext = buildContext, callback = callback)
                    .setIsSpace(true)
                    .build()
            }
            is NavTarget.SecureBackup -> {
                secureBackupEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = SecureBackupEntryPoint.Params(initialElement = navTarget.initialElement),
                    callback = object : SecureBackupEntryPoint.Callback {
                        override fun onDone() {
                            backstack.pop()
                        }
                    },
                )
            }
            NavTarget.Ftue -> {
                ftueEntryPoint.createNode(this, buildContext)
            }
            NavTarget.LinkNewDevice -> {
                val callback = object : LinkNewDeviceEntryPoint.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }
                }
                linkNewDeviceEntryPoint.createNode(this, buildContext, callback)
            }
            NavTarget.RoomDirectory -> {
                roomDirectoryEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = object : RoomDirectoryEntryPoint.Callback {
                        override fun navigateToRoom(roomDescription: RoomDescription) {
                            lifecycleScope.launch {
                                attachRoom(
                                    roomIdOrAlias = roomDescription.roomId.toRoomIdOrAlias(),
                                    roomDescription = roomDescription,
                                    trigger = JoinedRoomAnalyticsEvent.Trigger.RoomDirectory,
                                )
                            }
                        }
                    },
                )
            }
            is NavTarget.IncomingShare -> {
                shareEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = ShareEntryPoint.Params(shareIntentData = navTarget.shareIntentData),
                    callback = object : ShareEntryPoint.Callback {
                        override fun onDone(roomIds: List<RoomId>) {
                            // Remove the incoming share screen
                            backstack.pop()

                            // Navigate to the room if the text/media was shared to a single one
                            roomIds.singleOrNull()?.let { roomId ->
                                lifecycleScope.launch {
                                    // Wait until the incoming share screen is removed
                                    backstack.elements.first { it.lastOrNull()?.key?.navTarget !is NavTarget.IncomingShare }

                                    // Then attach the room
                                    attachRoom(roomId.toRoomIdOrAlias(), clearBackstack = false)
                                }
                            }
                        }
                    },
                )
            }
            is NavTarget.IncomingVerificationRequest -> {
                incomingVerificationEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    params = IncomingVerificationEntryPoint.Params(navTarget.data),
                    callback = object : IncomingVerificationEntryPoint.Callback {
                        override fun onDone() {
                            backstack.pop()
                        }
                    },
                )
            }
        }
    }

    suspend fun attachRoom(
        roomIdOrAlias: RoomIdOrAlias,
        serverNames: List<String> = emptyList(),
        trigger: JoinedRoomAnalyticsEvent.Trigger? = null,
        roomDescription: RoomDescription? = null,
        initialElement: RoomNavigationTarget = RoomNavigationTarget.Root(),
        clearBackstack: Boolean = false,
    ): RoomFlowNode {
        waitForNavTargetAttached { navTarget ->
            navTarget is NavTarget.Home
        }
        attachChild<RoomFlowNode> {
            val roomNavTarget = NavTarget.Room(
                roomIdOrAlias = roomIdOrAlias,
                serverNames = serverNames,
                roomDescription = roomDescription,
                trigger = trigger,
                initialElement = initialElement,
            )
            backstack.accept(AttachRoomOperation(roomNavTarget, clearBackstack))
        }

        // If we don't do this check, we might be returning while a previous node with the same type is still displayed
        // This means we may attach some new nodes to that one, which will be quickly replaced by the one instantiated above
        return waitForChildAttached<RoomFlowNode, NavTarget> {
            it is NavTarget.Room &&
                it.roomIdOrAlias == roomIdOrAlias &&
                it.initialElement == initialElement
        }
    }

    suspend fun attachUser(userId: UserId) {
        waitForNavTargetAttached { navTarget ->
            navTarget is NavTarget.Home
        }
        attachChild<Node> {
            backstack.push(
                NavTarget.UserProfile(
                    userId = userId,
                )
            )
        }
    }

    internal suspend fun attachIncomingShare(shareIntentData: ShareIntentData) {
        waitForNavTargetAttached { navTarget ->
            navTarget is NavTarget.Home
        }
        attachChild<Node> {
            backstack.push(
                NavTarget.IncomingShare(shareIntentData)
            )
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val colors by remember {
            enterpriseService.semanticColorsFlow(sessionId = matrixClient.sessionId)
        }.collectAsState(SemanticColorsLightDark.default)

        val currentMapTilerConfig = LocalMapTilerConfig.current
        val updatedMapTilerConfig by produceState(currentMapTilerConfig) {
            value = customMapTilerConfigProvider.get().getOrNull() ?: currentMapTilerConfig
        }

        ElementThemeApp(
            appPreferencesStore = appPreferencesStore,
            featureFlagService = featureFlagService,
            compoundLight = colors.light,
            compoundDark = colors.dark,
            buildMeta = buildMeta,
        ) {
            CompositionLocalProvider(LocalMapTilerConfig provides updatedMapTilerConfig) {
                val isOnline by syncService.isOnline.collectAsState()
                ConnectivityIndicatorContainer(
                    isOnline = isOnline,
                    modifier = modifier,
                ) { contentModifier ->
                    Box(modifier = contentModifier) {
                        val ftueState by ftueService.state.collectAsState()
                        BackstackView(transitionHandler = rememberLoggedInFlowTransitionHandler(backstack))
                        if (ftueState is FtueState.Complete) {
                            PermanentChild(permanentNavModel = permanentNavModel, navTarget = NavTarget.LoggedInPermanent)
                        }
                    }
                }
            }
        }
    }
}

@Parcelize
private class AttachRoomOperation(
    val roomTarget: LoggedInFlowNode.NavTarget.Room,
    val clearBackstack: Boolean,
) : BackStackOperation<LoggedInFlowNode.NavTarget> {
    /**
     * Returns a list containing last [count] elements that match [predicate] while preserving other elements.
     */
    private fun <T> List<T>.keepingLast(count: Int, predicate: (T) -> Boolean): List<T> {
        val matchingIndices = indices.filter { predicate(this[it]) }
        val indicesToRemove = matchingIndices.dropLast(count).toSet()
        return filterIndexed { index, _ -> index !in indicesToRemove }
    }

    override fun isApplicable(elements: NavElements<LoggedInFlowNode.NavTarget, BackStack.State>) = true

    override fun invoke(elements: BackStackElements<LoggedInFlowNode.NavTarget>): BackStackElements<LoggedInFlowNode.NavTarget> {
        return if (clearBackstack) {
            // Makes sure the room list target is alone in the backstack and stashed
            elements.mapNotNull { element ->
                if (element.key.navTarget == LoggedInFlowNode.NavTarget.Home) {
                    element.transitionTo(STASHED, this)
                } else {
                    null
                }
            } + BackStackElement(
                key = NavKey(roomTarget),
                fromState = CREATED,
                targetState = ACTIVE,
                operation = this
            )
        } else {
            val existingRoomElement = elements.find {
                val roomNavTarget = it.key.navTarget as? LoggedInFlowNode.NavTarget.Room
                roomNavTarget?.roomIdOrAlias == roomTarget.roomIdOrAlias
            }

            // Make sure the backstack of rooms can't grow indefinitely when opening permalinks.
            val roomElementCount = elements.count { it.key.navTarget is LoggedInFlowNode.NavTarget.Room }

            Timber.d("Current room nodes: $roomElementCount/$MAX_ROOM_NODE_COUNT")
            // Crate a new list keeping all the elements, but for Room ones just keep the last MAX_ROOM_NODE_COUNT
            val currentElements = elements.keepingLast(MAX_ROOM_NODE_COUNT) { element ->
                element.key.navTarget is LoggedInFlowNode.NavTarget.Room
            }

            // If the room already existed, remove it from the stack and add a new node at the end
            if (existingRoomElement != null) {
                currentElements.mapNotNull { element ->
                    if (element == existingRoomElement) {
                        null
                    } else {
                        element.transitionTo(STASHED, this)
                    }
                } + // Always create a new element, otherwise we wouldn't be navigating to the target event id or child node
                    BackStackElement(
                        key = NavKey(roomTarget),
                        fromState = CREATED,
                        targetState = ACTIVE,
                        operation = this
                    )
            } else {
                // Otherwise, just push the new node to the end of the backstack
                Push<LoggedInFlowNode.NavTarget>(roomTarget).invoke(currentElements)
            }
        }
    }
}
