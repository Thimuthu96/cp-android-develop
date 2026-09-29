/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package cloud.orelit.communicationplatform.features.messages.impl.messagecomposer

import android.net.Uri
import app.cash.turbine.ReceiveTurbine
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.location.api.LocationService
import cloud.orelit.communicationplatform.features.location.test.FakeLocationService
import cloud.orelit.communicationplatform.features.messages.impl.FakeMessagesNavigator
import cloud.orelit.communicationplatform.features.messages.impl.MessagesNavigator
import cloud.orelit.communicationplatform.features.messages.impl.draft.ComposerDraftService
import cloud.orelit.communicationplatform.features.messages.impl.draft.FakeComposerDraftService
import cloud.orelit.communicationplatform.features.messages.impl.messagecomposer.suggestions.SuggestionsProcessor
import cloud.orelit.communicationplatform.features.messages.impl.timeline.TimelineController
import cloud.orelit.communicationplatform.features.messages.impl.utils.FakeMentionSpanFormatter
import cloud.orelit.communicationplatform.features.messages.impl.utils.FakeTextPillificationHelper
import cloud.orelit.communicationplatform.features.messages.impl.utils.TextPillificationHelper
import cloud.orelit.communicationplatform.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import cloud.orelit.communicationplatform.libraries.featureflag.test.FakeFeatureFlagService
import cloud.orelit.communicationplatform.libraries.matrix.api.core.ThreadId
import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkBuilder
import cloud.orelit.communicationplatform.libraries.matrix.api.permalink.PermalinkParser
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.libraries.matrix.test.A_FAILURE_REASON
import cloud.orelit.communicationplatform.libraries.matrix.test.A_MESSAGE
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.permalink.FakePermalinkBuilder
import cloud.orelit.communicationplatform.libraries.matrix.test.permalink.FakePermalinkParser
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeJoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.InMemoryEventContentValidationCache
import cloud.orelit.communicationplatform.libraries.mediapickers.api.PickerProvider
import cloud.orelit.communicationplatform.libraries.mediapickers.test.FakePickerProvider
import cloud.orelit.communicationplatform.libraries.mediaupload.api.MediaOptimizationConfig
import cloud.orelit.communicationplatform.libraries.mediaupload.api.MediaPreProcessor
import cloud.orelit.communicationplatform.libraries.mediaupload.api.MediaSenderFactory
import cloud.orelit.communicationplatform.libraries.mediaupload.impl.DefaultMediaSender
import cloud.orelit.communicationplatform.libraries.mediaupload.test.FakeMediaOptimizationConfigProvider
import cloud.orelit.communicationplatform.libraries.mediaupload.test.FakeMediaPreProcessor
import cloud.orelit.communicationplatform.libraries.mediaviewer.test.FakeLocalMediaFactory
import cloud.orelit.communicationplatform.libraries.permissions.api.PermissionsPresenter
import cloud.orelit.communicationplatform.libraries.permissions.test.FakePermissionsPresenter
import cloud.orelit.communicationplatform.libraries.permissions.test.FakePermissionsPresenterFactory
import cloud.orelit.communicationplatform.libraries.preferences.api.store.SessionPreferencesStore
import cloud.orelit.communicationplatform.libraries.preferences.api.store.VideoCompressionPreset
import cloud.orelit.communicationplatform.libraries.preferences.test.InMemorySessionPreferencesStore
import cloud.orelit.communicationplatform.libraries.push.test.notifications.conversations.FakeNotificationConversationService
import cloud.orelit.communicationplatform.libraries.slashcommands.api.SlashCommand
import cloud.orelit.communicationplatform.libraries.slashcommands.api.SlashCommandService
import cloud.orelit.communicationplatform.libraries.slashcommands.test.FakeSlashCommandService
import cloud.orelit.communicationplatform.libraries.textcomposer.mentions.MentionSpanProvider
import cloud.orelit.communicationplatform.libraries.textcomposer.mentions.MentionSpanTheme
import cloud.orelit.communicationplatform.libraries.textcomposer.model.MessageComposerMode
import cloud.orelit.communicationplatform.services.analytics.test.FakeAnalyticsService
import cloud.orelit.communicationplatform.tests.testutils.WarmUpRule
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.lambda.value
import cloud.orelit.communicationplatform.tests.testutils.test
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class MessageComposerPresenterSlashCommandTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val pickerProvider = FakePickerProvider().apply {
        givenResult(mockk()) // Uri is not available in JVM, so the only way to have a non-null Uri is using Mockk
    }
    private val mediaPreProcessor = FakeMediaPreProcessor()
    private val snackbarDispatcher = SnackbarDispatcher()
    private val mockMediaUrl: Uri = mockk("localMediaUri")
    private val localMediaFactory = FakeLocalMediaFactory(mockMediaUrl)
    private val analyticsService = FakeAnalyticsService()
    private val notificationConversationService = FakeNotificationConversationService()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.isFullScreen).isFalse()
            assertThat(initialState.textEditorState.messageHtml()).isEmpty()
            assertThat(initialState.mode).isEqualTo(MessageComposerMode.Normal)
            assertThat(initialState.showAttachmentSourcePicker).isFalse()
            assertThat(initialState.canShareLocation).isTrue()
        }
    }

    @Test
    fun `present - slash command error sets failure`() = runTest {
        val presenter = createPresenter(
            slashCommandService = FakeSlashCommandService(
                parseResult = { _, _, _ -> SlashCommand.ErrorUnknownSlashCommand(A_FAILURE_REASON) }
            )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.textEditorState.setHtml(A_MESSAGE)
            initialState.eventSink(MessageComposerEvent.SendMessage)
            val errorState = awaitItem()
            assertThat(errorState.slashCommandAction.isFailure()).isTrue()
            assertThat(errorState.slashCommandAction.errorOrNull()?.message).isEqualTo(A_FAILURE_REASON)
            // Composer should not be reset when command is an error
            assertThat(errorState.textEditorState.messageHtml()).isEqualTo(A_MESSAGE)
            // Close the error
            errorState.eventSink(MessageComposerEvent.ClearSlashError)
            val finalState = awaitItem()
            assertThat(finalState.slashCommandAction.isUninitialized()).isTrue()
        }
    }

    @Test
    fun `present - slash command navigation ShowUser navigates to member and resets composer`() = runTest {
        val navigateToMember = lambdaRecorder<UserId, Unit> {}
        val navigator = FakeMessagesNavigator(navigateToMemberLambda = navigateToMember)
        val presenter = createPresenter(
            navigator = navigator,
            slashCommandService = FakeSlashCommandService(
                parseResult = { _, _, _ -> SlashCommand.ShowUser(A_USER_ID) }
            )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.textEditorState.setHtml(A_MESSAGE)
            initialState.eventSink(MessageComposerEvent.SendMessage)
            advanceUntilIdle()
            // navigation should be invoked and composer reset
            navigateToMember.assertions().isCalledOnce().with(value(A_USER_ID))
            assertThat(initialState.textEditorState.messageHtml()).isEmpty()
        }
    }

    @Test
    fun `present - slash command navigation DevTools navigates to developer settings and resets composer`() = runTest {
        val navigateToDev = lambdaRecorder<Unit> { }
        val navigator = FakeMessagesNavigator(navigateToDeveloperSettingsLambda = navigateToDev)
        val presenter = createPresenter(
            navigator = navigator,
            slashCommandService = FakeSlashCommandService(
                parseResult = { _, _, _ -> SlashCommand.DevTools }
            )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.textEditorState.setHtml(A_MESSAGE)
            initialState.eventSink(MessageComposerEvent.SendMessage)
            advanceUntilIdle()
            navigateToDev.assertions().isCalledOnce()
            assertThat(initialState.textEditorState.messageHtml()).isEmpty()
        }
    }

    @Test
    fun `present - slash command send message proceeds and resets composer`() = runTest {
        val presenter = createPresenter(
            slashCommandService = FakeSlashCommandService(
                parseResult = { _, _, _ -> SlashCommand.SendPlainText(A_MESSAGE) },
                proceedSendMessageResult = { _, _ -> Result.success(Unit) }
            )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.textEditorState.setHtml(A_MESSAGE)
            initialState.eventSink(MessageComposerEvent.SendMessage)
            advanceUntilIdle()
            // Composer reset after successful slash send
            assertThat(initialState.textEditorState.messageHtml()).isEmpty()
            // Ensure no failure
            assertThat(initialState.slashCommandAction.isFailure()).isFalse()
        }
    }

    @Test
    fun `present - slash command send message failure sets failure state`() = runTest {
        val presenter = createPresenter(
            slashCommandService = FakeSlashCommandService(
                parseResult = { _, _, _ -> SlashCommand.SendPlainText("A_MESSAGE") },
                proceedSendMessageResult = { _, _ -> Result.failure(Exception(A_FAILURE_REASON)) }
            )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.textEditorState.setHtml(A_MESSAGE)
            initialState.eventSink(MessageComposerEvent.SendMessage)
            val failureState = awaitItem()
            assertThat(failureState.slashCommandAction.isFailure()).isTrue()
            assertThat(failureState.slashCommandAction.errorOrNull()?.message).isEqualTo(A_FAILURE_REASON)
            // Clear the error
            failureState.eventSink(MessageComposerEvent.ClearSlashError)
            val finalState = awaitItem()
            assertThat(finalState.slashCommandAction.isUninitialized()).isTrue()
        }
    }

    @Test
    fun `present - slash command admin proceeds and resets state on success`() = runTest {
        val presenter = createPresenter(
            slashCommandService = FakeSlashCommandService(
                parseResult = { _, _, _ -> SlashCommand.BanUser(A_USER_ID, null) },
                proceedAdminResult = { _ -> Result.success(Unit) }
            )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.textEditorState.setHtml(A_MESSAGE)
            initialState.eventSink(MessageComposerEvent.SendMessage)
            val loadingState = awaitItem()
            assertThat(loadingState.slashCommandAction.isLoading()).isTrue()
            val successState = awaitItem()
            // After success, state should be Uninitialized
            assertThat(successState.slashCommandAction.isUninitialized()).isTrue()
            assertThat(successState.textEditorState.messageHtml()).isEmpty()
        }
    }

    @Test
    fun `present - slash command admin proceeds and emit failure on error`() = runTest {
        val presenter = createPresenter(
            slashCommandService = FakeSlashCommandService(
                parseResult = { _, _, _ -> SlashCommand.BanUser(A_USER_ID, null) },
                proceedAdminResult = { _ -> Result.failure(Exception(A_FAILURE_REASON)) }
            )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.textEditorState.setHtml(A_MESSAGE)
            initialState.eventSink(MessageComposerEvent.SendMessage)
            val loadingState = awaitItem()
            assertThat(loadingState.slashCommandAction.isLoading()).isTrue()
            val failureState = awaitItem()
            assertThat(failureState.slashCommandAction.isFailure()).isTrue()
            assertThat(failureState.slashCommandAction.errorOrNull()?.message).isEqualTo(A_FAILURE_REASON)
            // Clear error
            failureState.eventSink(MessageComposerEvent.ClearSlashError)
            val finalState = awaitItem()
            assertThat(finalState.slashCommandAction.isUninitialized()).isTrue()
        }
    }

    private fun TestScope.createPresenter(
        room: JoinedRoom = FakeJoinedRoom(
            typingNoticeResult = { Result.success(Unit) }
        ),
        timeline: Timeline = room.liveTimeline,
        navigator: MessagesNavigator = FakeMessagesNavigator(),
        pickerProvider: PickerProvider = this@MessageComposerPresenterSlashCommandTest.pickerProvider,
        locationService: LocationService = FakeLocationService(true),
        sessionPreferencesStore: SessionPreferencesStore = InMemorySessionPreferencesStore(),
        mediaPreProcessor: MediaPreProcessor = this@MessageComposerPresenterSlashCommandTest.mediaPreProcessor,
        snackbarDispatcher: SnackbarDispatcher = this@MessageComposerPresenterSlashCommandTest.snackbarDispatcher,
        permissionPresenter: PermissionsPresenter = FakePermissionsPresenter(),
        permalinkBuilder: PermalinkBuilder = FakePermalinkBuilder(),
        permalinkParser: PermalinkParser = FakePermalinkParser(),
        mentionSpanProvider: MentionSpanProvider = MentionSpanProvider(
            permalinkParser = permalinkParser,
            mentionSpanFormatter = FakeMentionSpanFormatter(),
            mentionSpanTheme = MentionSpanTheme(A_USER_ID)
        ),
        textPillificationHelper: TextPillificationHelper = FakeTextPillificationHelper(),
        isRichTextEditorEnabled: Boolean = true,
        draftService: ComposerDraftService = FakeComposerDraftService(),
        mediaOptimizationConfigProvider: FakeMediaOptimizationConfigProvider = FakeMediaOptimizationConfigProvider(),
        threadRoot: ThreadId? = null,
        slashCommandService: SlashCommandService = FakeSlashCommandService(),
        featureFlagService: FakeFeatureFlagService = FakeFeatureFlagService(),
    ) = MessageComposerPresenter(
        navigator = navigator,
        sessionCoroutineScope = this,
        threadRoot = threadRoot,
        room = room,
        mediaPickerProvider = pickerProvider,
        sessionPreferencesStore = sessionPreferencesStore,
        localMediaFactory = localMediaFactory,
        mediaSenderFactory = MediaSenderFactory { timelineMode ->
            DefaultMediaSender(
                preProcessor = mediaPreProcessor,
                room = room,
                timelineMode = timelineMode,
                mediaOptimizationConfigProvider = {
                    MediaOptimizationConfig(
                        compressImages = true,
                        videoCompressionPreset = VideoCompressionPreset.STANDARD
                    )
                }
            )
        },
        snackbarDispatcher = snackbarDispatcher,
        analyticsService = analyticsService,
        locationService = locationService,
        messageComposerContext = DefaultMessageComposerContext(),
        richTextEditorStateFactory = TestRichTextEditorStateFactory(),
        roomAliasSuggestionsDataSource = FakeRoomAliasSuggestionsDataSource(),
        permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionPresenter),
        permalinkParser = permalinkParser,
        permalinkBuilder = permalinkBuilder,
        timelineController = TimelineController(room, timeline),
        draftService = draftService,
        mentionSpanProvider = mentionSpanProvider,
        pillificationHelper = textPillificationHelper,
        suggestionsProcessor = SuggestionsProcessor(slashCommandService = slashCommandService),
        mediaOptimizationConfigProvider = mediaOptimizationConfigProvider,
        notificationConversationService = notificationConversationService,
        slashCommandService = slashCommandService,
        featureFlagService = featureFlagService,
        contentScannerService = { _, _ -> },
        contentValidationCache = InMemoryEventContentValidationCache(),
    ).apply {
        isTesting = true
        showTextFormatting = isRichTextEditorEnabled
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
        skipItems(1)
        return awaitItem()
    }
}
