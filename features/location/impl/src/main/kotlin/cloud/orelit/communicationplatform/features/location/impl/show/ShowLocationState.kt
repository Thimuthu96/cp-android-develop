/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.location.impl.show

import cloud.orelit.communicationplatform.features.enterprise.api.remoteconfig.MapTilerConfig
import cloud.orelit.communicationplatform.features.location.api.Location
import cloud.orelit.communicationplatform.features.location.impl.common.ui.LocationConstraintsDialogState
import cloud.orelit.communicationplatform.features.location.impl.common.ui.LocationMarkerData
import cloud.orelit.communicationplatform.features.location.impl.common.userlocation.UserLocationState
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.designsystem.components.PinVariant
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.room.location.AssetType
import kotlinx.collections.immutable.ImmutableList

data class ShowLocationState(
    val customMapTilerConfig: AsyncData<MapTilerConfig?>,
    val isLive: Boolean,
    val dialogState: LocationConstraintsDialogState,
    val locationShares: ImmutableList<LocationShareItem>,
    val focusedLocation: LocationShareItem?,
    val isTrackMyLocation: Boolean,
    val userLocationState: UserLocationState,
    val appName: String,
    val hideUserLocationPuck: Boolean,
    val eventSink: (ShowLocationEvent) -> Unit,
) {
    val isSheetDraggable = isLive && locationShares.isNotEmpty()
}

data class LocationShareItem(
    val userId: UserId,
    val displayName: String,
    val avatarData: AvatarData,
    val formattedTimestamp: String,
    val location: Location,
    val isLive: Boolean,
    val assetType: AssetType?,
    val isOwnUser: Boolean
) {
    val canStopSharing = isLive && isOwnUser
}

fun LocationShareItem.toMarkerData(): LocationMarkerData {
    val pinVariant = if (assetType == AssetType.PIN) {
        PinVariant.PinnedLocation
    } else {
        PinVariant.UserLocation(
            avatarData = avatarData,
            isLive = isLive,
        )
    }
    return LocationMarkerData(
        id = userId.value,
        location = location,
        variant = pinVariant,
    )
}
