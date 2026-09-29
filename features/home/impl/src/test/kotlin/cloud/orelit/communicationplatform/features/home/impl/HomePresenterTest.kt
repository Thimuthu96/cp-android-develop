/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl

import cloud.orelit.communicationplatform.features.home.impl.calendar.CalendarState
import cloud.orelit.communicationplatform.features.home.impl.calendar.aCalendarState
import cloud.orelit.communicationplatform.features.home.impl.roomlist.aRoomListState
import cloud.orelit.communicationplatform.features.home.impl.spaces.HomeSpacesState
import cloud.orelit.communicationplatform.features.home.impl.spaces.aHomeSpacesState
import cloud.orelit.communicationplatform.features.logout.api.direct.aDirectLogoutState
import cloud.orelit.communicationplatform.features.rageshake.api.RageshakeFeatureAvailability
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import cloud.orelit.communicationplatform.libraries.indicator.api.IndicatorService
import cloud.orelit.communicationplatform.libraries.indicator.test.FakeIndicatorService
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.sync.SyncService
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_AVATAR_URL
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EXCEPTION
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_NAME
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.test.sync.FakeSyncService
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.SessionStore
import cloud.orelit.communicationplatform.libraries.sessionstorage.test.InMemorySessionStore
import cloud.orelit.communicationplatform.libraries.sessionstorage.test.aSessionData
import cloud.orelit.communicationplatform.tests.testutils.WarmUpRule
import cloud.orelit.communicationplatform.tests.testutils.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class HomePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - should start with no user and then load user with success`() = runTest {
        val matrixClient = FakeMatrixClient(
            userDisplayName = null,
            userAvatarUrl = null,
        )
        matrixClient.givenGetProfileResult(matrixClient.sessionId, Result.success(MatrixUser(matrixClient.sessionId, A_USER_NAME, AN_AVATAR_URL)))
        val presenter = createHomePresenter(
            client = matrixClient,
            rageshakeFeatureAvailability = { flowOf(false) },
            sessionStore = InMemorySessionStore(
                initialList = listOf(
                    aSessionData(
                        sessionId = matrixClient.sessionId.value,
                        userDisplayName = null,
                        userAvatarUrl = null,
                    )
                ),
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.currentUserAndNeighbors.first()).isEqualTo(
                MatrixUser(A_USER_ID, null, null)
            )
            assertThat(initialState.canReportBug).isFalse()
            skipItems(1)
            val withUserState = awaitItem()
            assertThat(withUserState.currentUserAndNeighbors.first()).isEqualTo(
                MatrixUser(A_USER_ID, A_USER_NAME, AN_AVATAR_URL)
            )
            assertThat(withUserState.showAvatarIndicator).isFalse()
        }
    }

    @Test
    fun `present - can report bug`() = runTest {
        val presenter = createHomePresenter(
            rageshakeFeatureAvailability = { flowOf(true) },
            sessionStore = InMemorySessionStore(
                updateUserProfileResult = { _, _, _ -> },
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.canReportBug).isFalse()
            val finalState = awaitItem()
            assertThat(finalState.canReportBug).isTrue()
        }
    }

    @Test
    fun `present - show avatar indicator`() = runTest {
        val indicatorService = FakeIndicatorService()
        val presenter = createHomePresenter(
            indicatorService = indicatorService,
            sessionStore = InMemorySessionStore(
                updateUserProfileResult = { _, _, _ -> },
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.showAvatarIndicator).isFalse()
            indicatorService.setShowRoomListTopBarIndicator(true)
            val finalState = awaitItem()
            assertThat(finalState.showAvatarIndicator).isTrue()
        }
    }

    @Test
    fun `present - should start with no user and then load user with error`() = runTest {
        val matrixClient = FakeMatrixClient(
            userDisplayName = null,
            userAvatarUrl = null,
        )
        matrixClient.givenGetProfileResult(matrixClient.sessionId, Result.failure(AN_EXCEPTION))
        val presenter = createHomePresenter(
            client = matrixClient,
            sessionStore = InMemorySessionStore(
                updateUserProfileResult = { _, _, _ -> },
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.currentUserAndNeighbors.first()).isEqualTo(MatrixUser(matrixClient.sessionId))
            // No new state is coming
        }
    }

    @Test
    fun `present - NavigationBar change`() = runTest {
        val presenter = createHomePresenter(
            sessionStore = InMemorySessionStore(
                updateUserProfileResult = { _, _, _ -> },
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.currentHomeNavigationBarItem).isEqualTo(HomeNavigationBarItem.Chats)
            initialState.eventSink(HomeEvent.SelectHomeNavigationBarItem(HomeNavigationBarItem.Spaces))
            val finalState = awaitItem()
            assertThat(finalState.currentHomeNavigationBarItem).isEqualTo(HomeNavigationBarItem.Spaces)
            finalState.eventSink(HomeEvent.SelectHomeNavigationBarItem(HomeNavigationBarItem.Calendar))
            assertThat(awaitItem().currentHomeNavigationBarItem).isEqualTo(HomeNavigationBarItem.Calendar)
        }
    }
}

internal fun createHomePresenter(
    client: MatrixClient = FakeMatrixClient(),
    syncService: SyncService = FakeSyncService(),
    snackbarDispatcher: SnackbarDispatcher = SnackbarDispatcher(),
    rageshakeFeatureAvailability: RageshakeFeatureAvailability = RageshakeFeatureAvailability { flowOf(false) },
    indicatorService: IndicatorService = FakeIndicatorService(),
    homeSpacesPresenter: Presenter<HomeSpacesState> = Presenter { aHomeSpacesState() },
    calendarPresenter: Presenter<CalendarState> = Presenter { aCalendarState() },
    sessionStore: SessionStore = InMemorySessionStore(),
) = HomePresenter(
    client = client,
    syncService = syncService,
    snackbarDispatcher = snackbarDispatcher,
    indicatorService = indicatorService,
    roomListPresenter = { aRoomListState() },
    homeSpacesPresenter = homeSpacesPresenter,
    calendarPresenter = calendarPresenter,
    logoutPresenter = { aDirectLogoutState() },
    rageshakeFeatureAvailability = rageshakeFeatureAvailability,
    sessionStore = sessionStore,
)
