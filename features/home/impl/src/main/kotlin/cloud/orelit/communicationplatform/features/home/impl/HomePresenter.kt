/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import cloud.orelit.communicationplatform.features.home.impl.calendar.CalendarState
import cloud.orelit.communicationplatform.features.home.impl.roomlist.RoomListState
import cloud.orelit.communicationplatform.features.home.impl.spaces.HomeSpacesState
import cloud.orelit.communicationplatform.features.logout.api.direct.DirectLogoutState
import cloud.orelit.communicationplatform.features.rageshake.api.RageshakeFeatureAvailability
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import cloud.orelit.communicationplatform.libraries.indicator.api.IndicatorService
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.sync.SyncService
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.SessionStore
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@Inject
class HomePresenter(
    private val client: MatrixClient,
    private val syncService: SyncService,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val indicatorService: IndicatorService,
    private val roomListPresenter: Presenter<RoomListState>,
    private val homeSpacesPresenter: Presenter<HomeSpacesState>,
    private val calendarPresenter: Presenter<CalendarState>,
    private val logoutPresenter: Presenter<DirectLogoutState>,
    private val rageshakeFeatureAvailability: RageshakeFeatureAvailability,
    private val sessionStore: SessionStore,
) : Presenter<HomeState> {
    private val currentUserWithNeighborsBuilder = CurrentUserWithNeighborsBuilder()

    @Composable
    override fun present(): HomeState {
        val coroutineState = rememberCoroutineScope()
        val matrixUser by client.userProfile.collectAsState()
        val currentUserAndNeighbors by remember {
            combine(
                client.userProfile,
                sessionStore.sessionsFlow(),
                currentUserWithNeighborsBuilder::build,
            )
        }.collectAsState(initial = persistentListOf(matrixUser))
        val isOnline by syncService.isOnline.collectAsState()
        val canReportBug by remember { rageshakeFeatureAvailability.isAvailable() }.collectAsState(false)
        val roomListState = roomListPresenter.present()
        val homeSpacesState = homeSpacesPresenter.present()
        val calendarState = calendarPresenter.present()
        var currentHomeNavigationBarItemOrdinal by rememberSaveable { mutableIntStateOf(HomeNavigationBarItem.Chats.ordinal) }
        val currentHomeNavigationBarItem by remember {
            derivedStateOf {
                HomeNavigationBarItem.from(currentHomeNavigationBarItemOrdinal)
            }
        }
        LaunchedEffect(Unit) {
            // Force a refresh of the profile
            client.getUserProfile()
        }
        // Avatar indicator
        val showAvatarIndicator by indicatorService.showRoomListTopBarIndicator()
        val directLogoutState = logoutPresenter.present()

        fun handleEvent(event: HomeEvent) {
            when (event) {
                is HomeEvent.SelectHomeNavigationBarItem -> {
                    currentHomeNavigationBarItemOrdinal = event.item.ordinal
                }
                is HomeEvent.SwitchToAccount -> coroutineState.launch {
                    sessionStore.setLatestSession(event.sessionId.value)
                }
            }
        }

        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()
        return HomeState(
            currentUserAndNeighbors = currentUserAndNeighbors,
            showAvatarIndicator = showAvatarIndicator,
            hasNetworkConnection = isOnline,
            currentHomeNavigationBarItem = currentHomeNavigationBarItem,
            roomListState = roomListState,
            homeSpacesState = homeSpacesState,
            calendarState = calendarState,
            snackbarMessage = snackbarMessage,
            canReportBug = canReportBug,
            directLogoutState = directLogoutState,
            eventSink = ::handleEvent,
        )
    }
}
