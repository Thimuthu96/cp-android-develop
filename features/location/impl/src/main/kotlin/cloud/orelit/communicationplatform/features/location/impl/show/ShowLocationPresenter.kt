/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.location.impl.show

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import cloud.orelit.communicationplatform.features.enterprise.api.remoteconfig.CustomMapTilerConfigProvider
import cloud.orelit.communicationplatform.features.location.api.Location
import cloud.orelit.communicationplatform.features.location.api.ShowLocationMode
import cloud.orelit.communicationplatform.features.location.api.live.ActiveLiveLocationShareManager
import cloud.orelit.communicationplatform.features.location.api.live.isCurrentlySharing
import cloud.orelit.communicationplatform.features.location.impl.common.LocationConstraintsCheck
import cloud.orelit.communicationplatform.features.location.impl.common.MapDefaults
import cloud.orelit.communicationplatform.features.location.impl.common.SendLiveLocationPermissions
import cloud.orelit.communicationplatform.features.location.impl.common.actions.LocationActions
import cloud.orelit.communicationplatform.features.location.impl.common.checkLocationConstraints
import cloud.orelit.communicationplatform.features.location.impl.common.permissions.PermissionsEvent
import cloud.orelit.communicationplatform.features.location.impl.common.permissions.PermissionsPresenter
import cloud.orelit.communicationplatform.features.location.impl.common.permissions.PermissionsState
import cloud.orelit.communicationplatform.features.location.impl.common.toDialogState
import cloud.orelit.communicationplatform.features.location.impl.common.ui.LocationConstraintsDialogState
import cloud.orelit.communicationplatform.features.location.impl.common.userlocation.UserLocationState
import cloud.orelit.communicationplatform.features.location.impl.common.userlocation.asMapLibreLocation
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.core.coroutine.mapState
import cloud.orelit.communicationplatform.libraries.core.meta.BuildMeta
import cloud.orelit.communicationplatform.libraries.dateformatter.api.DateFormatter
import cloud.orelit.communicationplatform.libraries.dateformatter.api.DateFormatterMode
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.room.getBestName
import cloud.orelit.communicationplatform.libraries.matrix.api.room.joinedRoomMembers
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.services.toolbox.api.strings.StringProvider
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AssistedInject
class ShowLocationPresenter(
    @Assisted private val mode: ShowLocationMode,
    permissionsPresenterFactory: PermissionsPresenter.Factory,
    private val locationActions: LocationActions,
    private val buildMeta: BuildMeta,
    private val dateFormatter: DateFormatter,
    private val stringProvider: StringProvider,
    private val joinedRoom: JoinedRoom,
    private val liveLocationShareManager: ActiveLiveLocationShareManager,
    private val userLocationStateFactory: UserLocationState.Factory,
    private val customMapTilerConfigProvider: CustomMapTilerConfigProvider,
) : Presenter<ShowLocationState> {
    @AssistedFactory fun interface Factory {
        fun create(mode: ShowLocationMode): ShowLocationPresenter
    }

    private val permissionsPresenter = permissionsPresenterFactory.create(MapDefaults.permissions)

    @Composable
    override fun present(): ShowLocationState {
        val coroutineScope = rememberCoroutineScope()
        val permissionsState: PermissionsState = permissionsPresenter.present()
        var isTrackMyLocation by remember { mutableStateOf(false) }
        val appName by remember { derivedStateOf { buildMeta.applicationName } }
        var dialogState: LocationConstraintsDialogState by remember {
            mutableStateOf(LocationConstraintsDialogState.None)
        }

        val customMapTilerConfig by produceState(AsyncData.Loading()) {
            // Ignore errors
            value = AsyncData.Success(customMapTilerConfigProvider.get().getOrNull())
        }

        fun checkLocationConstraints() {
            val locationConstraints = checkLocationConstraints(
                permissionsState = permissionsState,
                locationActions = locationActions,
                // No need to check SendLiveLocationPermissions here
                sendLiveLocationPermissions = SendLiveLocationPermissions.GRANTED
            )
            if (locationConstraints is LocationConstraintsCheck.PermissionShouldBeRequested) {
                permissionsState.eventSink(PermissionsEvent.RequestPermissions)
            }
            isTrackMyLocation = locationConstraints is LocationConstraintsCheck.Success
            dialogState = locationConstraints.toDialogState()
        }

        LaunchedEffect(permissionsState) {
            checkLocationConstraints()
        }

        fun handleEvent(event: ShowLocationEvent) {
            when (event) {
                is ShowLocationEvent.Share -> {
                    locationActions.share(event.location, null)
                }
                is ShowLocationEvent.TrackMyLocation -> {
                    if (event.enabled) {
                        checkLocationConstraints()
                    } else {
                        isTrackMyLocation = false
                    }
                }
                ShowLocationEvent.DismissDialog -> dialogState = LocationConstraintsDialogState.None
                ShowLocationEvent.OpenAppSettings -> {
                    locationActions.openAppSettings()
                    dialogState = LocationConstraintsDialogState.None
                }
                ShowLocationEvent.OpenLocationSettings -> {
                    locationActions.openLocationSettings()
                    dialogState = LocationConstraintsDialogState.None
                }
                ShowLocationEvent.RequestPermissions -> permissionsState.eventSink(PermissionsEvent.RequestPermissions)
                ShowLocationEvent.StopLocationSharing -> coroutineScope.launch {
                    liveLocationShareManager.stopShare(joinedRoom.roomId)
                }
            }
        }

        val locationShares = when (mode) {
            is ShowLocationMode.Static -> {
                remember {
                    val relativeTime = dateFormatter.format(timestamp = mode.timestamp, mode = DateFormatterMode.Full, useRelative = true)
                    val formattedTimestamp = stringProvider.getString(
                        CommonStrings.screen_static_location_sheet_timestamp_description,
                        relativeTime
                    )
                    persistentListOf(
                        LocationShareItem(
                            userId = mode.senderId,
                            displayName = mode.senderName,
                            avatarData = AvatarData(
                                id = mode.senderId.value,
                                name = mode.senderName,
                                url = mode.senderAvatarUrl,
                                size = AvatarSize.UserListItem,
                            ),
                            formattedTimestamp = formattedTimestamp,
                            location = mode.location,
                            isLive = false,
                            assetType = mode.assetType,
                            isOwnUser = mode.senderId == joinedRoom.sessionId
                        )
                    )
                }
            }
            is ShowLocationMode.Live -> {
                produceState(persistentListOf()) {
                    val comparator = LiveLocationShareComparator(currentUser = joinedRoom.sessionId)
                    val liveLocationSharesFlow = joinedRoom.subscribeToLiveLocationShares()
                    val membersStateFlow = joinedRoom.membersStateFlow.mapState { it.joinedRoomMembers() }
                    combine(liveLocationSharesFlow, membersStateFlow) { liveShares, members ->
                        liveShares.sortedWith(comparator).mapNotNull { share ->
                            val lastLocation = share.lastLocation ?: return@mapNotNull null
                            val location = Location.fromGeoUri(lastLocation.geoUri) ?: return@mapNotNull null
                            val member = members.find { it.userId == share.userId }
                            val displayName = member?.getBestName() ?: share.userId.value
                            val avatarUrl = member?.avatarUrl
                            val relativeTime = dateFormatter.format(timestamp = lastLocation.timestamp, mode = DateFormatterMode.Full, useRelative = true)
                            val formattedTimestamp = stringProvider.getString(
                                CommonStrings.screen_static_location_sheet_timestamp_description,
                                relativeTime
                            )
                            LocationShareItem(
                                userId = share.userId,
                                displayName = displayName,
                                avatarData = AvatarData(
                                    id = share.userId.value,
                                    name = displayName,
                                    url = avatarUrl,
                                    size = AvatarSize.UserListItem,
                                ),
                                formattedTimestamp = formattedTimestamp,
                                location = location,
                                isLive = true,
                                assetType = lastLocation.assetType,
                                isOwnUser = share.userId == joinedRoom.sessionId
                            )
                        }.toImmutableList()
                    }.collect { value = it }
                }.value
            }
        }

        val updatedLocationShares by rememberUpdatedState(locationShares)
        val focusedLocation by remember {
            derivedStateOf {
                when (mode) {
                    is ShowLocationMode.Static -> updatedLocationShares.firstOrNull()
                    is ShowLocationMode.Live -> updatedLocationShares.firstOrNull { it.userId == mode.senderId }
                }
            }
        }
        val isCurrentlySharing by liveLocationShareManager.isCurrentlySharing(roomId = joinedRoom.roomId).collectAsState()
        val hideUserLocationPuck = mode is ShowLocationMode.Live && isCurrentlySharing
        val userLocationState = if (hideUserLocationPuck) {
            // When sharing with this device, use the user LocationShareItem as source of data instead of the device.
            val ownLocationShare by remember { derivedStateOf { updatedLocationShares.find { it.isOwnUser }?.location?.asMapLibreLocation() } }
            UserLocationState(ownLocationShare)
        } else {
            userLocationStateFactory.create(hasLocationPermission = permissionsState.isAnyGranted)
        }
        return ShowLocationState(
            customMapTilerConfig = customMapTilerConfig,
            dialogState = dialogState,
            locationShares = locationShares,
            focusedLocation = focusedLocation,
            isTrackMyLocation = isTrackMyLocation,
            userLocationState = userLocationState,
            isLive = mode is ShowLocationMode.Live,
            appName = appName,
            hideUserLocationPuck = hideUserLocationPuck,
            eventSink = ::handleEvent,
        )
    }
}
