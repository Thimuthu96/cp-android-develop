/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.viewer

import android.net.Uri
import app.cash.turbine.ReceiveTurbine
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaSource
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID_2
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SESSION_ID_2
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.media.FakeMatrixMediaLoader
import cloud.orelit.communicationplatform.libraries.matrix.test.media.aMediaSource
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeBaseRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeJoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.room.powerlevels.FakeRoomPermissions
import cloud.orelit.communicationplatform.libraries.matrix.test.timeline.FakeTimeline
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.NoopEventContentValidationCache
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.AvatarInfo
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.MediaViewerEntryPoint
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.anApkMediaInfo
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.local.LocalMediaFactory
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.R
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.datasource.FakeMediaGalleryDataSource
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.datasource.MediaGalleryDataSource
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.details.MediaBottomSheetState
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.model.GroupedMediaItems
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.model.aMediaItemImage
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.model.aMediaItemLoadingIndicator
import cloud.orelit.communicationplatform.libraries.mediaviewer.test.FakeLocalMediaActions
import cloud.orelit.communicationplatform.libraries.mediaviewer.test.FakeLocalMediaFactory
import cloud.orelit.communicationplatform.services.toolbox.test.systemclock.FakeSystemClock
import cloud.orelit.communicationplatform.tests.testutils.WarmUpRule
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.lambda.value
import cloud.orelit.communicationplatform.tests.testutils.test
import cloud.orelit.communicationplatform.tests.testutils.testCoroutineDispatchers
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private val TESTED_MEDIA_INFO = anApkMediaInfo(
    senderId = A_USER_ID,
)

@Suppress("LargeClass")
class MediaViewerPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val mockMediaUri: Uri = mockk("localMediaUri")
    private val localMediaFactory = FakeLocalMediaFactory(mockMediaUri)
    private val aUrl = "aUrl"

    private val anImage = aMediaItemImage(
        mediaSourceUrl = aUrl,
    )
    private val aBackwardLoadingIndicator = aMediaItemLoadingIndicator(
        direction = Timeline.PaginationDirection.BACKWARDS
    )
    private val aForwardLoadingIndicator = aMediaItemLoadingIndicator(
        direction = Timeline.PaginationDirection.FORWARDS
    )

    @Test
    fun `present - initial state null Event`() = runTest {
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                    roomPermissions = FakeRoomPermissions(
                        canRedactOwn = true
                    ),
                )
            )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.listData).isEmpty()
            assertThat(initialState.currentIndex).isEqualTo(0)
            assertThat(initialState.snackbarMessage).isNull()
            assertThat(initialState.canShowInfo).isTrue()
            assertThat(initialState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state cannot show info`() = runTest {
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            inputs = MediaViewerEntryPoint.Params.Avatar(
                avatarInfo = AvatarInfo(filename = "avatar.png"),
                mediaSource = aMediaSource(),
                thumbnailSource = null,
                blurHash = null,
            ),
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                    roomPermissions = FakeRoomPermissions(
                        canRedactOwn = true
                    ),
                )
            )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.listData).isEmpty()
            assertThat(initialState.currentIndex).isEqualTo(0)
            assertThat(initialState.snackbarMessage).isNull()
            assertThat(initialState.canShowInfo).isFalse()
            assertThat(initialState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state Event`() = runTest {
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            eventId = AN_EVENT_ID,
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                    roomPermissions = FakeRoomPermissions(
                        canRedactOwn = true
                    ),
                )
            )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.listData).isEmpty()
            assertThat(initialState.currentIndex).isEqualTo(0)
            assertThat(initialState.snackbarMessage).isNull()
            assertThat(initialState.canShowInfo).isTrue()
            assertThat(initialState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state Event from other`() = runTest {
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            eventId = AN_EVENT_ID,
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                    sessionId = A_SESSION_ID_2,
                    roomPermissions = FakeRoomPermissions(
                        canRedactOther = false
                    ),
                )
            )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.listData).isEmpty()
            assertThat(initialState.currentIndex).isEqualTo(0)
            assertThat(initialState.snackbarMessage).isNull()
            assertThat(initialState.canShowInfo).isTrue()
            assertThat(initialState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - data source update`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage()
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.listData).isEmpty()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitFirstItem()
            assertThat(updatedState.listData).hasSize(1)
            val item = updatedState.listData.first() as MediaViewerPageData.MediaViewerData
            assertThat(item.eventId).isNull()
            assertThat(item.mediaInfo).isEqualTo(anImage.mediaInfo)
            assertThat(item.mediaSource).isEqualTo(anImage.mediaSource)
            assertThat(item.thumbnailSource).isEqualTo(anImage.thumbnailSource)
            assertThat(item.downloadedMedia.value).isEqualTo(AsyncData.Uninitialized)
        }
    }

    @Test
    fun `present - load media`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvent.LoadMedia(
                    aMediaViewerPageData(
                        mediaSource = MediaSource(aUrl)
                    )
                )
            )
        }
    }

    @Test
    fun `present - open info`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            mediaGalleryDataSource = mediaGalleryDataSource,
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                    roomPermissions = FakeRoomPermissions(
                        canRedactOwn = true
                    ),
                )
            )
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvent.OpenInfo(
                    aMediaViewerPageData(
                        mediaSource = MediaSource(aUrl)
                    )
                )
            )
            val withInfoState = awaitItem()
            assertThat(withInfoState.mediaBottomSheetState).isInstanceOf(MediaBottomSheetState.Details::class.java)
            withInfoState.eventSink(
                MediaViewerEvent.CloseBottomSheet
            )
            val finalState = awaitItem()
            assertThat(finalState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
        }
    }

    @Test
    fun `present - clear loading error`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvent.ClearLoadingError(
                    aMediaViewerPageData(
                        mediaSource = MediaSource(aUrl)
                    )
                )
            )
        }
    }

    @Test
    fun `present - share`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvent.Share(
                    aMediaViewerPageData(
                        mediaSource = MediaSource(aUrl)
                    )
                )
            )
        }
    }

    @Test
    fun `present - save on disk`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvent.SaveOnDisk(
                    aMediaViewerPageData(
                        mediaSource = MediaSource(aUrl)
                    )
                )
            )
        }
    }

    @Test
    fun `present - open with`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvent.OpenWith(
                    aMediaViewerPageData(
                        mediaSource = MediaSource(aUrl)
                    )
                )
            )
        }
    }

    @Test
    fun `present - delete and cancel`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvent.ConfirmDelete(
                    eventId = AN_EVENT_ID,
                    data = aMediaViewerPageData(
                        mediaSource = MediaSource(aUrl)
                    )
                )
            )
            val withBottomSheetState = awaitItem()
            assertThat(withBottomSheetState.mediaBottomSheetState).isInstanceOf(MediaBottomSheetState.DeleteConfirmation::class.java)
            withBottomSheetState.eventSink(
                MediaViewerEvent.CloseBottomSheet
            )
            val finalState = awaitItem()
            assertThat(finalState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
        }
    }

    @Test
    fun `present - delete`() = runTest {
        val redactEventLambda = lambdaRecorder<EventOrTransactionId, String?, Result<Unit>> { _, _ ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply {
            this.redactEventLambda = redactEventLambda
        }
        val onItemDeletedLambda = lambdaRecorder<Unit> { }
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            room = FakeJoinedRoom(
                liveTimeline = timeline,
                baseRoom = FakeBaseRoom(
                    roomPermissions = FakeRoomPermissions(
                        canRedactOwn = true
                    ),
                ),
            ),
            mediaGalleryDataSource = mediaGalleryDataSource,
            mediaViewerNavigator = FakeMediaViewerNavigator(
                onItemDeletedLambda = onItemDeletedLambda
            )
        )
        val anImage = aMediaItemImage(
            eventId = AN_EVENT_ID,
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvent.ConfirmDelete(
                    eventId = AN_EVENT_ID,
                    data = aMediaViewerPageData(
                        mediaSource = MediaSource(aUrl)
                    )
                )
            )
            val withBottomSheetState = awaitItem()
            assertThat(withBottomSheetState.mediaBottomSheetState).isInstanceOf(MediaBottomSheetState.DeleteConfirmation::class.java)
            updatedState.eventSink(
                MediaViewerEvent.Delete(
                    eventId = AN_EVENT_ID,
                )
            )
            val finalState = awaitItem()
            assertThat(finalState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
            redactEventLambda.assertions()
                .isCalledOnce()
                .with(
                    value(AN_EVENT_ID.toEventOrTransactionId()),
                    value(null),
                )
            onItemDeletedLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - on navigate to`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        val anImage2 = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage, anImage2),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvent.OnNavigateTo(1)
            )
            val finalState = awaitItem()
            assertThat(finalState.currentIndex).isEqualTo(1)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - snackbar displayed when there is no more items forward images and videos`() {
        `present - snackbar displayed when there is no more items forward`(
            mode = MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos(timelineMode = Timeline.Mode.Media),
            expectedSnackbarResId = R.string.screen_media_details_no_more_media_to_show,
        )
    }

    @Test
    fun `present - snackbar displayed when there is no more items forward files and audio`() {
        `present - snackbar displayed when there is no more items forward`(
            mode = MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios(timelineMode = Timeline.Mode.Media),
            expectedSnackbarResId = R.string.screen_media_details_no_more_files_to_show,
        )
    }

    private fun `present - snackbar displayed when there is no more items forward`(
        mode: MediaViewerEntryPoint.MediaViewerMode,
        expectedSnackbarResId: Int,
    ) = runTest {
        val image = anImage.copy(eventId = AN_EVENT_ID)
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            initialData = AsyncData.Success(
                if (mode is MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios) {
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(),
                        fileItems = persistentListOf(aForwardLoadingIndicator, image, aBackwardLoadingIndicator),
                    )
                } else {
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(aForwardLoadingIndicator, image, aBackwardLoadingIndicator),
                        fileItems = persistentListOf(),
                    )
                }
            ),
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            eventId = AN_EVENT_ID,
            localMediaFactory = localMediaFactory,
            mode = mode,
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        presenter.test {
            val updatedState = awaitFirstItem()

            advanceUntilIdle()
            runCurrent()
            // User navigate to the first item (forward loading indicator)
            updatedState.eventSink(
                MediaViewerEvent.OnNavigateTo(0)
            )
            // data source claims that there is no more items to load forward
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    if (mode is MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios) {
                        GroupedMediaItems(
                            imageAndVideoItems = persistentListOf(),
                            fileItems = persistentListOf(image, aBackwardLoadingIndicator),
                        )
                    } else {
                        GroupedMediaItems(
                            imageAndVideoItems = persistentListOf(image, aBackwardLoadingIndicator),
                            fileItems = persistentListOf(),
                        )
                    }
                )
            )
            skipItems(1)
            val stateWithSnackbar = awaitItem()
            assertThat(stateWithSnackbar.snackbarMessage!!.messageResId).isEqualTo(expectedSnackbarResId)
        }
    }

    @Test
    fun `present - snackbar displayed when there is no more items backward images and videos`() {
        `present - snackbar displayed when there is no more items backward`(
            mode = MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos(timelineMode = Timeline.Mode.Media),
            expectedSnackbarResId = R.string.screen_media_details_no_more_media_to_show,
        )
    }

    @Test
    fun `present - snackbar displayed when there is no more items backward files and audio`() {
        `present - snackbar displayed when there is no more items backward`(
            mode = MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios(timelineMode = Timeline.Mode.Media),
            expectedSnackbarResId = R.string.screen_media_details_no_more_files_to_show,
        )
    }

    private fun `present - snackbar displayed when there is no more items backward`(
        mode: MediaViewerEntryPoint.MediaViewerMode,
        expectedSnackbarResId: Int,
    ) = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            mode = mode,
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    if (mode is MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios) {
                        GroupedMediaItems(
                            imageAndVideoItems = persistentListOf(),
                            fileItems = persistentListOf(aForwardLoadingIndicator, anImage, aBackwardLoadingIndicator),
                        )
                    } else {
                        GroupedMediaItems(
                            imageAndVideoItems = persistentListOf(aForwardLoadingIndicator, anImage, aBackwardLoadingIndicator),
                            fileItems = persistentListOf(),
                        )
                    }
                )
            )
            val updatedState = awaitItem()
            // User navigate to the last item (backward loading indicator)
            updatedState.eventSink(
                MediaViewerEvent.OnNavigateTo(2)
            )
            skipItems(1)
            // data source claims that there is no more items to load backward
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    if (mode is MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios) {
                        GroupedMediaItems(
                            imageAndVideoItems = persistentListOf(),
                            fileItems = persistentListOf(aForwardLoadingIndicator, anImage),
                        )
                    } else {
                        GroupedMediaItems(
                            imageAndVideoItems = persistentListOf(aForwardLoadingIndicator, anImage),
                            fileItems = persistentListOf(),
                        )
                    }
                )
            )
            skipItems(1)
            val stateWithSnackbar = awaitItem()
            assertThat(stateWithSnackbar.snackbarMessage!!.messageResId).isEqualTo(expectedSnackbarResId)
        }
    }

    @Test
    fun `present - no snackbar displayed when there is no more items but not displaying a loading item`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
            initialData = AsyncData.Success(
                GroupedMediaItems(
                    imageAndVideoItems = persistentListOf(aForwardLoadingIndicator, anImage, anImage.copy(eventId = AN_EVENT_ID_2), aBackwardLoadingIndicator),
                    fileItems = persistentListOf(),
                )
            )
        )
        val presenter = createMediaViewerPresenter(
            eventId = AN_EVENT_ID,
            localMediaFactory = localMediaFactory,
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        presenter.test {
            val updatedState = awaitItem()
            // User navigate to the media
            updatedState.eventSink(
                MediaViewerEvent.OnNavigateTo(1)
            )
            skipItems(1)
            // data source claims that there is no more items to load at all
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val finalState = awaitItem()
            assertThat(finalState.snackbarMessage).isNull()
        }
    }

    @Test
    fun `present - load more`() = runTest {
        val loadMoreLambda = lambdaRecorder<Timeline.PaginationDirection, Unit> { }
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
            loadMoreLambda = loadMoreLambda,
        )
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvent.LoadMore(Timeline.PaginationDirection.BACKWARDS)
            )
            loadMoreLambda.assertions().isCalledOnce().with(value(Timeline.PaginationDirection.BACKWARDS))
        }
    }

    @Test
    fun `present - receiving loading items with different timestamps emits different items too`() = runTest {
        val loadMoreLambda = lambdaRecorder<Timeline.PaginationDirection, Unit> { }
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
            loadMoreLambda = loadMoreLambda,
        )
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(aForwardLoadingIndicator, anImage, aBackwardLoadingIndicator),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()

            // Get the exact same items, but with new timestamps for the loading indicators
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(
                            aForwardLoadingIndicator.copy(timestamp = 1234L),
                            anImage,
                            aBackwardLoadingIndicator.copy(timestamp = 1234L),
                        ),
                        fileItems = persistentListOf(),
                    )
                )
            )

            // We should get a new list of items, which should not be equal to the previous one
            assertThat(updatedState.listData).isNotEqualTo(awaitItem().listData)
        }
    }

    @Test
    fun `present - view in timeline hides the bottom sheet and invokes the navigator`() = runTest {
        val onViewInTimelineClickLambda = lambdaRecorder<EventId, Unit> { }
        val navigator = FakeMediaViewerNavigator(
            onViewInTimelineClickLambda = onViewInTimelineClickLambda,
        )
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            mediaViewerNavigator = navigator,
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                    roomPermissions = FakeRoomPermissions(
                        canRedactOwn = true
                    ),
                ),
            )
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(MediaViewerEvent.OpenInfo(aMediaViewerPageData()))
            skipItems(1)
            val withBottomSheetState = awaitItem()
            assertThat(withBottomSheetState.mediaBottomSheetState).isInstanceOf(MediaBottomSheetState.Details::class.java)
            initialState.eventSink(MediaViewerEvent.ViewInTimeline(AN_EVENT_ID))
            val finalState = awaitItem()
            assertThat(finalState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
            onViewInTimelineClickLambda.assertions().isCalledOnce().with(value(AN_EVENT_ID))
        }
    }

    @Test
    fun `present - forward hides the bottom sheet and invokes the navigator`() = runTest {
        val onForwardClickLambda = lambdaRecorder<EventId, Boolean, Unit> { _, _ -> }
        val navigator = FakeMediaViewerNavigator(
            onForwardClickLambda = onForwardClickLambda,
        )
        val presenter = createMediaViewerPresenter(
            localMediaFactory = localMediaFactory,
            mediaViewerNavigator = navigator,
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                    roomPermissions = FakeRoomPermissions(
                        canRedactOwn = true
                    ),
                ),
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(MediaViewerEvent.OpenInfo(aMediaViewerPageData()))
            skipItems(1)
            val withBottomSheetState = awaitItem()
            assertThat(withBottomSheetState.mediaBottomSheetState).isInstanceOf(MediaBottomSheetState.Details::class.java)
            initialState.eventSink(MediaViewerEvent.Forward(AN_EVENT_ID))
            val finalState = awaitItem()
            assertThat(finalState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
            onForwardClickLambda.assertions().isCalledOnce()
                .with(value(AN_EVENT_ID), value(false))
        }
    }

    @Test
    fun `present - forward from pinned events hides the bottom sheet and invokes the navigator`() = runTest {
        val onForwardClickLambda = lambdaRecorder<EventId, Boolean, Unit> { _, _ -> }
        val navigator = FakeMediaViewerNavigator(
            onForwardClickLambda = onForwardClickLambda,
        )
        val presenter = createMediaViewerPresenter(
            mode = MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios(timelineMode = Timeline.Mode.PinnedEvents),
            localMediaFactory = localMediaFactory,
            mediaViewerNavigator = navigator,
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                    roomPermissions = FakeRoomPermissions(
                        canRedactOwn = true
                    ),
                ),
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(MediaViewerEvent.OpenInfo(aMediaViewerPageData()))
            skipItems(1)
            val withBottomSheetState = awaitItem()
            assertThat(withBottomSheetState.mediaBottomSheetState).isInstanceOf(MediaBottomSheetState.Details::class.java)
            initialState.eventSink(MediaViewerEvent.Forward(AN_EVENT_ID))
            val finalState = awaitItem()
            assertThat(finalState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
            onForwardClickLambda.assertions().isCalledOnce()
                .with(value(AN_EVENT_ID), value(true))
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
        return awaitItem()
    }
}

internal fun TestScope.createMediaViewerPresenter(
    localMediaFactory: LocalMediaFactory,
    inputs: MediaViewerEntryPoint.Params? = null,
    eventId: EventId? = null,
    mode: MediaViewerEntryPoint.MediaViewerMode = MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos(Timeline.Mode.Media),
    matrixMediaLoader: FakeMatrixMediaLoader = FakeMatrixMediaLoader(),
    localMediaActions: FakeLocalMediaActions = FakeLocalMediaActions(),
    mediaGalleryDataSource: MediaGalleryDataSource = FakeMediaGalleryDataSource(
        startLambda = { },
    ),
    mediaViewerNavigator: MediaViewerNavigator = FakeMediaViewerNavigator(),
    room: JoinedRoom = FakeJoinedRoom(
        liveTimeline = FakeTimeline(),
    ),
): MediaViewerPresenter {
    val actualInputs = inputs ?: createMediaViewerEntryPointParams(eventId = eventId, mode = mode)
    val actualMode = when (actualInputs) {
        is MediaViewerEntryPoint.Params.Avatar -> MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos(Timeline.Mode.Media)
        is MediaViewerEntryPoint.Params.EventGallery -> MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos(Timeline.Mode.Media)
        is MediaViewerEntryPoint.Params.RoomMedia -> actualInputs.mode
    }
    return MediaViewerPresenter(
        inputs = actualInputs,
        navigator = mediaViewerNavigator,
        dataSource = MediaViewerDataSource(
            coroutineScope = backgroundScope,
            mode = actualMode,
            dispatcher = testCoroutineDispatchers().computation,
            galleryDataSource = mediaGalleryDataSource,
            mediaLoader = matrixMediaLoader,
            localMediaFactory = localMediaFactory,
            systemClock = FakeSystemClock(),
            pagerKeysHandler = PagerKeysHandler(),
            contentScannerService = { _, _ -> },
            contentValidationCache = NoopEventContentValidationCache(),
        ),
        room = room,
        localMediaActions = localMediaActions,
    )
}

internal fun createMediaViewerEntryPointParams(
    eventId: EventId? = null,
    mode: MediaViewerEntryPoint.MediaViewerMode = MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios(timelineMode = Timeline.Mode.Media),
) = MediaViewerEntryPoint.Params.RoomMedia(
    mode = mode,
    eventId = eventId,
    mediaInfo = TESTED_MEDIA_INFO,
    mediaSource = aMediaSource(),
    thumbnailSource = null,
    blurHash = null,
)
