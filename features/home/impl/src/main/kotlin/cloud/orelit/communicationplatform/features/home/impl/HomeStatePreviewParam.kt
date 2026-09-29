/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import cloud.orelit.communicationplatform.features.home.impl.calendar.CalendarState
import cloud.orelit.communicationplatform.features.home.impl.calendar.aCalendarState
import cloud.orelit.communicationplatform.features.home.impl.roomlist.RoomListState
import cloud.orelit.communicationplatform.features.home.impl.roomlist.RoomListStatePreviewParam
import cloud.orelit.communicationplatform.features.home.impl.roomlist.aRoomListState
import cloud.orelit.communicationplatform.features.home.impl.roomlist.aRoomsContentState
import cloud.orelit.communicationplatform.features.home.impl.roomlist.generateRoomListRoomSummaryList
import cloud.orelit.communicationplatform.features.home.impl.spaces.HomeSpacesState
import cloud.orelit.communicationplatform.features.home.impl.spaces.aHomeSpacesState
import cloud.orelit.communicationplatform.features.logout.api.direct.DirectLogoutState
import cloud.orelit.communicationplatform.features.logout.api.direct.aDirectLogoutState
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarMessage
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList

open class HomeStatePreviewParam : PreviewParameterProvider<HomeState> {
    override val values: Sequence<HomeState>
        get() = sequenceOf(
            aHomeState(),
            aHomeState(hasNetworkConnection = false),
            aHomeState(snackbarMessage = SnackbarMessage(CommonStrings.common_verification_complete)),
            aHomeState(
                roomListState = aRoomListState(
                    // Add more rooms to see the blur effect under the NavigationBar
                    contentState = aRoomsContentState(
                        summaries = generateRoomListRoomSummaryList(),
                    )
                ),
                // For the bottom nav bar to be visible in the preview, the user must be member of at least one space
                homeSpacesState = aHomeSpacesState(),
            ),
            aHomeState(
                currentHomeNavigationBarItem = HomeNavigationBarItem.Spaces,
            ),
            aHomeState(
                currentHomeNavigationBarItem = HomeNavigationBarItem.Calendar,
            ),
        ) + RoomListStatePreviewParam().values.map {
            aHomeState(roomListState = it)
        } + aHomeState(
            currentHomeNavigationBarItem = HomeNavigationBarItem.Spaces,
            homeSpacesState = aHomeSpacesState(
                spaceRooms = emptyList(),
            ),
        )
}

internal fun aHomeState(
    matrixUser: MatrixUser = MatrixUser(userId = UserId("@id:domain"), displayName = "User#1"),
    currentUserAndNeighbors: List<MatrixUser> = listOf(matrixUser),
    showAvatarIndicator: Boolean = false,
    hasNetworkConnection: Boolean = true,
    snackbarMessage: SnackbarMessage? = null,
    currentHomeNavigationBarItem: HomeNavigationBarItem = HomeNavigationBarItem.Chats,
    roomListState: RoomListState = aRoomListState(),
    homeSpacesState: HomeSpacesState = aHomeSpacesState(),
    calendarState: CalendarState = aCalendarState(),
    canReportBug: Boolean = true,
    directLogoutState: DirectLogoutState = aDirectLogoutState(),
    eventSink: (HomeEvent) -> Unit = {}
) = HomeState(
    currentUserAndNeighbors = currentUserAndNeighbors.toImmutableList(),
    showAvatarIndicator = showAvatarIndicator,
    hasNetworkConnection = hasNetworkConnection,
    snackbarMessage = snackbarMessage,
    canReportBug = canReportBug,
    directLogoutState = directLogoutState,
    currentHomeNavigationBarItem = currentHomeNavigationBarItem,
    roomListState = roomListState,
    homeSpacesState = homeSpacesState,
    calendarState = calendarState,
    eventSink = eventSink,
)
