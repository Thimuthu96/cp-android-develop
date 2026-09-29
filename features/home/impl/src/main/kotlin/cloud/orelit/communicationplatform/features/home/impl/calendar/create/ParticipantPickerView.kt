/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.create

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cloud.orelit.communicationplatform.compound.theme.ElementTheme
import cloud.orelit.communicationplatform.compound.tokens.generated.CompoundIcons
import cloud.orelit.communicationplatform.features.home.impl.R
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarSize
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.CircularProgressIndicator
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Icon
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.IconButton
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Scaffold
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.SearchField
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TextButton
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.TopAppBar
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.CheckableUserRow
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.CheckableUserRowData
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.SelectedUsersRowList
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getAvatarData
import cloud.orelit.communicationplatform.libraries.matrix.ui.model.getBestName
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.libraries.usersearch.api.UserSearchResult
import kotlinx.coroutines.flow.drop

/** Picks the participants of an event, displayed on top of the event form. */
@Composable
internal fun ParticipantPickerView(
    form: NewEventForm,
    picker: ParticipantPickerState,
    eventSink: (EventFormEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler { eventSink(EventFormEvent.CloseParticipantPicker) }
    val queryState = rememberTextFieldState(initialText = picker.query)
    val latestEventSink by rememberUpdatedState(eventSink)
    LaunchedEffect(queryState) {
        // The first value is the query the field was created with, the presenter already has it.
        snapshotFlow { queryState.text.toString() }.drop(1).collect { latestEventSink(EventFormEvent.SetParticipantQuery(it)) }
    }

    Scaffold(
        modifier = modifier.background(ElementTheme.colors.bgCanvasDefault),
        topBar = {
            TopAppBar(
                titleStr = stringResource(R.string.screen_calendar_event_add_people),
                navigationIcon = {
                    IconButton(onClick = { eventSink(EventFormEvent.CloseParticipantPicker) }) {
                        Icon(
                            imageVector = CompoundIcons.ArrowLeft(),
                            contentDescription = stringResource(CommonStrings.action_back),
                        )
                    }
                },
                actions = {
                    TextButton(
                        text = stringResource(CommonStrings.action_done),
                        onClick = { eventSink(EventFormEvent.CloseParticipantPicker) },
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding(),
        ) {
            SearchField(
                state = queryState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = stringResource(CommonStrings.common_search_for_someone),
            )
            if (form.participants.isNotEmpty()) {
                SelectedUsersRowList(
                    selectedUsers = form.participants,
                    onUserRemove = { eventSink(EventFormEvent.RemoveParticipant(it)) },
                    autoScroll = true,
                    modifier = Modifier.padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                )
            }
            Box(Modifier.fillMaxSize()) {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(picker.results, key = { it.matrixUser.userId.value }) { result ->
                        ParticipantResultRow(
                            result = result,
                            isSelected = form.participants.any { it.userId == result.matrixUser.userId },
                            onToggle = { eventSink(EventFormEvent.ToggleParticipant(result.matrixUser)) },
                        )
                    }
                }
                when {
                    picker.isSearching && picker.results.isEmpty() -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    picker.query.isNotBlank() && !picker.isSearching && picker.results.isEmpty() -> Text(
                        text = stringResource(CommonStrings.common_no_results),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        style = ElementTheme.typography.fontBodyLgRegular,
                        color = ElementTheme.colors.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ParticipantResultRow(
    result: UserSearchResult,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    val user = result.matrixUser
    val avatarData = user.getAvatarData(AvatarSize.UserListItem)
    CheckableUserRow(
        checked = isSelected,
        onCheckedChange = { onToggle() },
        data = if (result.isUnresolved) {
            CheckableUserRowData.Unresolved(avatarData = avatarData, id = user.userId.value)
        } else {
            CheckableUserRowData.Resolved(
                avatarData = avatarData,
                name = user.getBestName(),
                subtext = user.userId.value.takeUnless { user.displayName.isNullOrEmpty() },
            )
        },
    )
}

@PreviewsDayNight
@Composable
internal fun ParticipantPickerViewPreview() = ElementPreview {
    val state = anEventFormState(withParticipantPicker = true)
    ParticipantPickerView(
        form = state.form,
        picker = requireNotNull(state.participantPicker),
        eventSink = {},
    )
}
