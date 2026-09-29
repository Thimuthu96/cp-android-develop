/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.slashcommands.impl

import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.di.RoomScope
import cloud.orelit.communicationplatform.libraries.featureflag.api.FeatureFlagService
import cloud.orelit.communicationplatform.libraries.featureflag.api.FeatureFlags
import cloud.orelit.communicationplatform.libraries.matrix.api.HomeserverCapabilitiesProvider
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.libraries.preferences.api.store.AppPreferencesStore
import cloud.orelit.communicationplatform.libraries.slashcommands.api.SlashCommand
import cloud.orelit.communicationplatform.libraries.slashcommands.api.SlashCommandService
import cloud.orelit.communicationplatform.libraries.slashcommands.api.SlashCommandSuggestion
import cloud.orelit.communicationplatform.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

@ContributesBinding(RoomScope::class)
class DefaultSlashCommandService(
    private val commandParser: CommandParser,
    private val commandExecutor: CommandExecutor,
    private val stringProvider: StringProvider,
    private val appPreferencesStore: AppPreferencesStore,
    private val featureFlagService: FeatureFlagService,
    private val capabilitiesProvider: HomeserverCapabilitiesProvider,
) : SlashCommandService {
    override suspend fun getSuggestions(
        text: String,
        isInThread: Boolean,
    ): List<SlashCommandSuggestion> {
        if (!featureFlagService.isFeatureEnabled(FeatureFlags.SlashCommand)) return emptyList()
        val isDeveloperModeEnabled = appPreferencesStore.isDeveloperModeEnabledFlow().first()
        return Command.entries
            .asSequence()
            .filter { it.startsWith(text) }
            .filter { !isInThread || it.isAllowedInThread }
            .filter { !it.isDevCommand || isDeveloperModeEnabled }
            // Don't include the change display name commands if the user can't change their display name
            .run {
                val canUserChangeDisplayName = withTimeoutOrNull(5.seconds) {
                    capabilitiesProvider.canChangeDisplayName().getOrNull()
                } ?: false
                if (!canUserChangeDisplayName) {
                    filterNot { it == Command.CHANGE_DISPLAY_NAME || it == Command.CHANGE_DISPLAY_NAME_FOR_ROOM }
                } else {
                    this
                }
            }
            // Don't include the change avatar commands if the user can't change their avatar url
            .run {
                val canUserChangeAvatar = withTimeoutOrNull(5.seconds) {
                    capabilitiesProvider.canChangeAvatarUrl().getOrNull()
                } ?: false
                if (!canUserChangeAvatar) {
                    filterNot { it == Command.CHANGE_AVATAR || it == Command.CHANGE_AVATAR_FOR_ROOM }
                } else {
                    this
                }
            }
            .map {
                SlashCommandSuggestion(
                    command = it.command,
                    parameters = it.parameters,
                    description = stringProvider.getString(it.description),
                )
            }
            .toList()
    }

    override suspend fun parse(
        textMessage: CharSequence,
        formattedMessage: String?,
        isInThreadTimeline: Boolean,
    ): SlashCommand {
        return commandParser.parseSlashCommand(
            textMessage = textMessage,
            formattedMessage = formattedMessage,
            isInThreadTimeline = isInThreadTimeline,
        )
    }

    override suspend fun proceedSendMessage(
        slashCommand: SlashCommand.SlashCommandSendMessage,
        timeline: Timeline,
    ): Result<Unit> {
        return commandExecutor.proceedSendMessage(
            slashCommand = slashCommand,
            timeline = timeline,
        )
    }

    override suspend fun proceedAdmin(
        slashCommand: SlashCommand.SlashCommandAdmin,
    ): Result<Unit> {
        return commandExecutor.proceedAdmin(
            slashCommand = slashCommand,
        )
    }
}
