/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalHazeMaterialsApi::class)

package cloud.orelit.communicationplatform.features.home.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.home.impl.calendar.CalendarEvent
import cloud.orelit.communicationplatform.features.home.impl.calendar.CalendarView
import cloud.orelit.communicationplatform.features.home.impl.calendar.create.EventFormView
import cloud.orelit.communicationplatform.features.home.impl.calendar.detail.CalendarEventDetailsView
import cloud.orelit.communicationplatform.features.home.impl.components.HomeTopBar
import cloud.orelit.communicationplatform.features.home.impl.components.RoomListContentView
import cloud.orelit.communicationplatform.features.home.impl.components.RoomListMenuAction
import cloud.orelit.communicationplatform.features.home.impl.model.RoomListRoomSummary
import cloud.orelit.communicationplatform.features.home.impl.roomlist.RoomListContextMenu
import cloud.orelit.communicationplatform.features.home.impl.roomlist.RoomListDeclineInviteMenu
import cloud.orelit.communicationplatform.features.home.impl.roomlist.RoomListEvent
import cloud.orelit.communicationplatform.features.home.impl.roomlist.RoomListState
import cloud.orelit.communicationplatform.features.home.impl.search.GlobalSearchEvent
import cloud.orelit.communicationplatform.features.home.impl.search.GlobalSearchView
import cloud.orelit.communicationplatform.features.home.impl.search.RoomListSearchView
import cloud.orelit.communicationplatform.features.home.impl.spacefilters.SpaceFiltersEvent
import cloud.orelit.communicationplatform.features.home.impl.spacefilters.SpaceFiltersState
import cloud.orelit.communicationplatform.features.home.impl.spacefilters.SpaceFiltersView
import cloud.orelit.communicationplatform.features.home.impl.spaces.HomeSpacesView
import cloud.orelit.communicationplatform.libraries.androidutils.throttler.FirstThrottler
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.FloatingActionButton
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.HorizontalFloatingToolbar
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.HorizontalFloatingToolbarItem
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.HorizontalFloatingToolbarSeparator
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Scaffold
import cloud.orelit.communicationplatform.libraries.designsystem.utils.lazyColumnContentPadding
import cloud.orelit.communicationplatform.libraries.designsystem.utils.scaffoldScrollableContentInsets
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarHost
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch

@Composable
fun HomeView(
    homeState: HomeState,
    onRoomClick: (RoomId, EventId?) -> Unit,
    onSettingsClick: () -> Unit,
    onSetUpRecoveryClick: () -> Unit,
    onConfirmRecoveryKeyClick: () -> Unit,
    onStartChatClick: () -> Unit,
    onCreateSpaceClick: () -> Unit,
    onRoomSettingsClick: (roomId: RoomId) -> Unit,
    onMenuActionClick: (RoomListMenuAction) -> Unit,
    onReportRoomClick: (roomId: RoomId) -> Unit,
    onDeclineInviteAndBlockUser: (roomSummary: RoomListRoomSummary) -> Unit,
    acceptDeclineInviteView: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leaveRoomView: @Composable () -> Unit,
) {
    val state: RoomListState = homeState.roomListState
    val coroutineScope = rememberCoroutineScope()
    val firstThrottler = remember { FirstThrottler(300, coroutineScope) }
    Box(modifier) {
        if (state.contextMenu is RoomListState.ContextMenu.Shown) {
            RoomListContextMenu(
                contextMenu = state.contextMenu,
                canReportRoom = state.canReportRoom,
                eventSink = state.eventSink,
                onRoomSettingsClick = onRoomSettingsClick,
                onReportRoomClick = onReportRoomClick,
            )
        }
        if (state.declineInviteMenu is RoomListState.DeclineInviteMenu.Shown) {
            RoomListDeclineInviteMenu(
                menu = state.declineInviteMenu,
                canReportRoom = state.canReportRoom,
                eventSink = state.eventSink,
                onDeclineAndBlockClick = onDeclineInviteAndBlockUser,
            )
        }

        leaveRoomView()

        HomeScaffold(
            state = homeState,
            onSetUpRecoveryClick = onSetUpRecoveryClick,
            onConfirmRecoveryKeyClick = onConfirmRecoveryKeyClick,
            onRoomClick = { roomId -> if (firstThrottler.canHandle()) onRoomClick(roomId, null) },
            onOpenSettings = { if (firstThrottler.canHandle()) onSettingsClick() },
            onStartChatClick = { if (firstThrottler.canHandle()) onStartChatClick() },
            onCreateSpaceClick = { if (firstThrottler.canHandle()) onCreateSpaceClick() },
            onMenuActionClick = onMenuActionClick,
        )

        if (state.globalSearchState.isEnabled) {
            GlobalSearchView(
                state = state.globalSearchState,
                onSelectSearchResult = { roomId, eventId -> if (firstThrottler.canHandle()) onRoomClick(roomId, eventId) },
                modifier = Modifier
                    .fillMaxSize()
                    .background(ElementTheme.colors.bgCanvasDefault),
            )
        } else {
            // This overlaid view will only be visible when state.displaySearchResults is true
            RoomListSearchView(
                state = state.searchState,
                eventSink = state.eventSink,
                hideInvitesAvatars = state.hideInvitesAvatars,
                onRoomClick = { roomId -> if (firstThrottler.canHandle()) onRoomClick(roomId, null) },
                modifier = Modifier
                    .fillMaxSize()
                    .background(ElementTheme.colors.bgCanvasDefault)
            )
        }

        // The details of an event are a sheet over the calendar, so the day behind them stays visible.
        homeState.calendarState.eventDetails?.let { eventDetails ->
            CalendarEventDetailsView(
                state = eventDetails,
                onOpenChat = { roomId -> if (firstThrottler.canHandle()) onRoomClick(roomId, null) },
            )
        }

        // The form to create an event covers the whole screen, the navigation bar included.
        homeState.calendarState.eventForm?.let { eventForm ->
            EventFormView(
                state = eventForm,
                modifier = Modifier.fillMaxSize(),
            )
        }

        acceptDeclineInviteView()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScaffold(
    state: HomeState,
    onSetUpRecoveryClick: () -> Unit,
    onConfirmRecoveryKeyClick: () -> Unit,
    onRoomClick: (RoomId) -> Unit,
    onOpenSettings: () -> Unit,
    onStartChatClick: () -> Unit,
    onCreateSpaceClick: () -> Unit,
    onMenuActionClick: (RoomListMenuAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onRoomClick(room: RoomListRoomSummary) {
        onRoomClick(room.roomId)
    }

    val appBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(appBarState)
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)
    val roomListState: RoomListState = state.roomListState

    BackHandler(enabled = state.isBackHandlerEnabled) {
        if (state.currentHomeNavigationBarItem != HomeNavigationBarItem.Chats) {
            state.eventSink(HomeEvent.SelectHomeNavigationBarItem(HomeNavigationBarItem.Chats))
        } else {
            val spaceFiltersState = state.roomListState.spaceFiltersState
            if (spaceFiltersState is SpaceFiltersState.Selected) {
                spaceFiltersState.eventSink(SpaceFiltersEvent.Selected.ClearSelection)
            }
        }
    }

    val hazeState = rememberHazeState()
    val roomsLazyListState = rememberLazyListState()
    val spacesLazyListState = rememberLazyListState()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            HomeTopBar(
                selectedNavigationItem = state.currentHomeNavigationBarItem,
                currentUserAndNeighbors = state.currentUserAndNeighbors,
                showAvatarIndicator = state.showAvatarIndicator,
                areSearchResultsDisplayed = if (roomListState.globalSearchState.isEnabled) {
                    roomListState.globalSearchState.isSearchActive
                } else {
                    roomListState.searchState.isSearchActive
                },
                onToggleSearch = {
                    if (roomListState.globalSearchState.isEnabled) {
                        roomListState.globalSearchState.eventSink(GlobalSearchEvent.ToggleSearchVisibility)
                    } else {
                        roomListState.eventSink(RoomListEvent.ToggleSearchResults)
                    }
                },
                onMenuActionClick = onMenuActionClick,
                onOpenSettings = onOpenSettings,
                onAccountSwitch = {
                    state.eventSink(HomeEvent.SwitchToAccount(it))
                },
                scrollBehavior = scrollBehavior,
                displayFilters = state.displayRoomListFilters,
                filtersState = roomListState.filtersState,
                spaceFiltersState = roomListState.spaceFiltersState,
                calendarState = state.calendarState,
                canReportBug = state.canReportBug,
                modifier = Modifier.hazeEffect(
                    state = hazeState,
                    style = HazeMaterials.thick(),
                )
            )
        },
        floatingActionButton = {
            val coroutineScope = rememberCoroutineScope()
            HomeBottomBar(
                // The Scaffold uses top-only insets so the scrollable content can go edge-to-edge behind the
                // navigation bar, so the floating toolbar has to apply the bottom inset itself to avoid overlapping it.
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                currentHomeNavigationBarItem = state.currentHomeNavigationBarItem,
                onItemClick = { item ->
                    // scroll to top if selecting the same item
                    if (item == state.currentHomeNavigationBarItem) {
                        val lazyListStateTarget = when (item) {
                            HomeNavigationBarItem.Chats -> roomsLazyListState
                            HomeNavigationBarItem.Spaces -> spacesLazyListState
                            HomeNavigationBarItem.Calendar -> null
                        }
                        if (lazyListStateTarget == null) {
                            // The calendar is not a list, selecting it again brings it back to today.
                            state.calendarState.eventSink(CalendarEvent.GoToToday)
                        } else {
                            coroutineScope.launch {
                                if (lazyListStateTarget.firstVisibleItemIndex > 10) {
                                    lazyListStateTarget.scrollToItem(10)
                                }
                                // Also reset the scrollBehavior height offset as it's not triggered by programmatic scrolls
                                scrollBehavior.state.heightOffset = 0f
                                lazyListStateTarget.animateScrollToItem(0)
                            }
                        }
                    } else {
                        state.eventSink(HomeEvent.SelectHomeNavigationBarItem(item))
                    }
                },
                floatingActionButton = {
                    when (state.currentHomeNavigationBarItem) {
                        HomeNavigationBarItem.Chats -> {
                            HomeFloatingActionButton(onStartChatClick, CommonStrings.action_create_room)
                        }
                        HomeNavigationBarItem.Spaces -> {
                            HomeFloatingActionButton(onCreateSpaceClick, CommonStrings.action_create_space)
                        }
                        HomeNavigationBarItem.Calendar -> {
                            HomeFloatingActionButton(
                                onClick = { state.calendarState.eventSink(CalendarEvent.NewEvent) },
                                contentDescription = R.string.screen_calendar_new_event,
                            )
                        }
                    }
                },
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        contentWindowInsets = scaffoldScrollableContentInsets,
        content = { padding ->
            val outerPadding = PaddingValues(
                start = padding.calculateStartPadding(LocalLayoutDirection.current),
                end = padding.calculateEndPadding(LocalLayoutDirection.current),
                // Remove these two lines once https://issuetracker.google.com/issues/436432313 has been fixed
                bottom = padding.calculateBottomPadding(),
                top = padding.calculateTopPadding()
            )
            val contentPadding = PaddingValues(
                bottom = 96.dp,
            )
            when (state.currentHomeNavigationBarItem) {
                HomeNavigationBarItem.Chats -> {
                    RoomListContentView(
                        contentState = roomListState.contentState,
                        filtersState = roomListState.filtersState,
                        spaceFiltersState = roomListState.spaceFiltersState,
                        lazyListState = roomsLazyListState,
                        hideInvitesAvatars = roomListState.hideInvitesAvatars,
                        eventSink = roomListState.eventSink,
                        onSetUpRecoveryClick = onSetUpRecoveryClick,
                        onConfirmRecoveryKeyClick = onConfirmRecoveryKeyClick,
                        onRoomClick = ::onRoomClick,
                        onCreateRoomClick = onStartChatClick,
                        contentPadding = lazyColumnContentPadding + contentPadding,
                        modifier = Modifier
                            .padding(outerPadding)
                            .consumeWindowInsets(outerPadding)
                            .hazeSource(state = hazeState)
                    )
                    SpaceFiltersView(roomListState.spaceFiltersState)
                }
                HomeNavigationBarItem.Spaces -> {
                    HomeSpacesView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(outerPadding)
                            .consumeWindowInsets(outerPadding)
                            .hazeSource(state = hazeState),
                        contentPadding = lazyColumnContentPadding + contentPadding,
                        state = state.homeSpacesState,
                        lazyListState = spacesLazyListState,
                        onSpaceClick = { spaceId ->
                            onRoomClick(spaceId)
                        },
                        onCreateSpaceClick = onCreateSpaceClick,
                        // TODO use actual callbacks for this
                        onExploreClick = {},
                    )
                }
                HomeNavigationBarItem.Calendar -> {
                    CalendarView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(outerPadding)
                            .consumeWindowInsets(outerPadding)
                            .hazeSource(state = hazeState),
                        state = state.calendarState,
                        contentPadding = lazyColumnContentPadding + contentPadding,
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    )
}

@Composable
private fun HomeFloatingActionButton(
    onClick: () -> Unit,
    contentDescription: Int,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = CompoundIcons.Plus(),
            contentDescription = stringResource(id = contentDescription),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HomeBottomBar(
    currentHomeNavigationBarItem: HomeNavigationBarItem,
    onItemClick: (HomeNavigationBarItem) -> Unit,
    modifier: Modifier = Modifier,
    floatingActionButton: (@Composable () -> Unit)?,
) {
    HorizontalFloatingToolbar(
        floatingActionButton = floatingActionButton,
        modifier = modifier
            .zIndex(1f),
    ) {
        HomeNavigationBarItem.entries.forEachIndexed { index, item ->
            if (index > 0) {
                HorizontalFloatingToolbarSeparator()
            }
            val isSelected = currentHomeNavigationBarItem == item
            HorizontalFloatingToolbarItem(
                icon = item.icon(isSelected),
                tooltipLabel = stringResource(item.labelRes),
                isSelected = isSelected,
                onClick = { onItemClick(item) },
            )
        }
    }
}

internal fun RoomListRoomSummary.contentType() = displayType.ordinal

@PreviewsDayNight
@Composable
internal fun HomeViewPreview(@PreviewParameter(HomeStatePreviewParam::class) state: HomeState) = ElementPreview {
    HomeView(
        homeState = state,
        onRoomClick = { _, _ -> },
        onSettingsClick = {},
        onSetUpRecoveryClick = {},
        onConfirmRecoveryKeyClick = {},
        onStartChatClick = {},
        onCreateSpaceClick = {},
        onRoomSettingsClick = {},
        onReportRoomClick = {},
        onMenuActionClick = {},
        onDeclineInviteAndBlockUser = {},
        acceptDeclineInviteView = {},
        leaveRoomView = {}
    )
}

@Preview
@Composable
internal fun HomeViewA11yPreview() = ElementPreview {
    HomeView(
        homeState = aHomeState(),
        onRoomClick = { _, _ -> },
        onSettingsClick = {},
        onSetUpRecoveryClick = {},
        onConfirmRecoveryKeyClick = {},
        onStartChatClick = {},
        onCreateSpaceClick = {},
        onRoomSettingsClick = {},
        onReportRoomClick = {},
        onMenuActionClick = {},
        onDeclineInviteAndBlockUser = {},
        acceptDeclineInviteView = {},
        leaveRoomView = {}
    )
}
