/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.datasource

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.androidutils.filesize.FakeFileSizeFormatter
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.core.mimetype.MimeTypes
import cloud.orelit.communicationplatform.libraries.dateformatter.test.FakeDateFormatter
import cloud.orelit.communicationplatform.libraries.matrix.api.media.ImageInfo
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MediaSource
import cloud.orelit.communicationplatform.libraries.matrix.api.media.ThumbnailInfo
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.MatrixTimelineItem
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FormattedBody
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.ImageMessageType
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MessageFormat
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EXCEPTION
import cloud.orelit.communicationplatform.libraries.matrix.test.A_UNIQUE_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeJoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.timeline.FakeTimeline
import cloud.orelit.communicationplatform.libraries.matrix.test.timeline.aMessageContent
import cloud.orelit.communicationplatform.libraries.matrix.test.timeline.anEventTimelineItem
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.A_BLUR_HASH
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.NoopContentValidationState
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.NoopEventContentValidationCache
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.MediaInfo
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.model.GroupedMediaItems
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.model.MediaItem
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.model.aMediaItemImage
import cloud.orelit.communicationplatform.libraries.mediaviewer.test.util.FileExtensionExtractorWithoutValidation
import cloud.orelit.communicationplatform.tests.testutils.WarmUpRule
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.lambda.value
import cloud.orelit.communicationplatform.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class TimelineMediaGalleryDataSourceTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `test - not started TimelineMediaGalleryDataSource emits no events`() = runTest {
        val fakeTimeline = FakeTimeline()
        val sut = createTimelineMediaGalleryDataSource(
            room = FakeJoinedRoom(
                createTimelineResult = { Result.success(fakeTimeline) },
                roomCoroutineScope = backgroundScope,
            )
        )
        sut.groupedMediaItemsFlow().test {
            // Also, loadMore and deleteItem should be no-op
            sut.loadMore(Timeline.PaginationDirection.BACKWARDS)
            sut.deleteItem(AN_EVENT_ID)
            expectNoEvents()
        }
    }

    @Test
    fun `test - getLastData should return the previous emitted data`() {
        val fakeTimeline = FakeTimeline()
        runTest {
            val sut = createTimelineMediaGalleryDataSource(
                room = FakeJoinedRoom(
                    createTimelineResult = { Result.success(fakeTimeline) },
                    roomCoroutineScope = backgroundScope,
                )
            )
            sut.start(backgroundScope)
            assertThat(sut.getLastData()).isEqualTo(AsyncData.Uninitialized)
            sut.groupedMediaItemsFlow().test {
                assertThat(awaitItem().isLoading()).isTrue()
                assertThat(sut.getLastData().isLoading()).isTrue()
                assertThat(awaitItem()).isEqualTo(
                    AsyncData.Success(
                        GroupedMediaItems(
                            imageAndVideoItems = persistentListOf(),
                            fileItems = persistentListOf(),
                        )
                    )
                )
                assertThat(sut.getLastData().isSuccess()).isTrue()
                // Also test that starting again should have no effect
                sut.start(backgroundScope)
            }
        }
        // Ensure that the timeline has been closed on flow completion
        assertThat(fakeTimeline.closeCounter).isEqualTo(1)
    }

    @Test
    fun `test - load more should call the timeline paginate method`() = runTest {
        val paginateLambdaRecorder =
            lambdaRecorder<Timeline.PaginationDirection, Result<Boolean>> { _ ->
                Result.success(true)
            }
        val fakeTimeline = FakeTimeline().apply {
            paginateLambda = paginateLambdaRecorder
        }
        val sut = createTimelineMediaGalleryDataSource(
            room = FakeJoinedRoom(
                createTimelineResult = { Result.success(fakeTimeline) },
                roomCoroutineScope = backgroundScope,
            )
        )
        sut.start(backgroundScope)
        sut.groupedMediaItemsFlow().test {
            skipItems(2)
            sut.loadMore(Timeline.PaginationDirection.BACKWARDS)
            paginateLambdaRecorder.assertions().isCalledOnce().with(value(Timeline.PaginationDirection.BACKWARDS))
        }
    }

    @Test
    fun `test - delete item should call the timeline redact method`() = runTest {
        val redactEventLambdaRecorder =
            lambdaRecorder<EventOrTransactionId, String?, Result<Unit>> { _, _ ->
                Result.success(Unit)
            }
        val fakeTimeline = FakeTimeline().apply {
            redactEventLambda = redactEventLambdaRecorder
        }
        val sut = createTimelineMediaGalleryDataSource(
            room = FakeJoinedRoom(
                createTimelineResult = { Result.success(fakeTimeline) },
                roomCoroutineScope = backgroundScope,
            )
        )
        sut.start(backgroundScope)
        sut.groupedMediaItemsFlow().test {
            skipItems(2)
            sut.deleteItem(AN_EVENT_ID)
            redactEventLambdaRecorder.assertions().isCalledOnce().with(
                value(AN_EVENT_ID.toEventOrTransactionId()),
                value(null),
            )
        }
    }

    @Test
    fun `test - failing to load timeline should emit an error`() = runTest {
        val sut = createTimelineMediaGalleryDataSource(
            room = FakeJoinedRoom(
                createTimelineResult = { Result.failure(AN_EXCEPTION) },
                roomCoroutineScope = backgroundScope,
            )
        )
        sut.start(backgroundScope)
        sut.groupedMediaItemsFlow().test {
            assertThat(awaitItem().isLoading()).isTrue()
            assertThat(sut.getLastData().isLoading()).isTrue()
            assertThat(awaitItem()).isEqualTo(
                AsyncData.Failure<GroupedMediaItems>(AN_EXCEPTION)
            )
        }
    }

    @Test
    fun `test - failing to load a focused timeline keeps the item that was opened`() = runTest {
        val room = FakeJoinedRoom(
            createTimelineResult = { Result.failure(AN_EXCEPTION) },
            roomCoroutineScope = backgroundScope,
        )
        val initialMediaItem = aMediaItemImage()
        val sut = createTimelineMediaGalleryDataSource(
            room = room,
            mediaTimeline = FocusedMediaTimeline(
                room = room,
                eventId = AN_EVENT_ID,
                onlyPinnedEvents = false,
                initialMediaItem = initialMediaItem,
            ),
        )
        sut.start(backgroundScope)
        sut.groupedMediaItemsFlow().test {
            val item = awaitItem()
            assertThat(item.isSuccess()).isTrue()
            assertThat(item.dataOrNull()?.imageAndVideoItems).contains(initialMediaItem)
            expectNoEvents()
        }
        assertThat(sut.getLastData().isFailure()).isFalse()
    }

    @Test
    fun `test - when timeline emits new data, the flow emits the data`() = runTest {
        val timelineItems = MutableStateFlow<List<MatrixTimelineItem>>(emptyList())
        val fakeTimeline = FakeTimeline(
            timelineItems = timelineItems,
        )
        val sut = createTimelineMediaGalleryDataSource(
            room = FakeJoinedRoom(
                createTimelineResult = { Result.success(fakeTimeline) },
                roomCoroutineScope = backgroundScope,
            )
        )
        sut.start(backgroundScope)
        sut.groupedMediaItemsFlow().test {
            assertThat(awaitItem().isLoading()).isTrue()
            assertThat(sut.getLastData().isLoading()).isTrue()
            assertThat(awaitItem()).isEqualTo(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(),
                        fileItems = persistentListOf(),
                    )
                )
            )
            timelineItems.emit(
                listOf(
                    MatrixTimelineItem.Event(
                        uniqueId = A_UNIQUE_ID,
                        event = anEventTimelineItem(
                            content = aMessageContent(
                                messageType = ImageMessageType(
                                    filename = "body.jpg",
                                    caption = "body.jpg caption",
                                    formattedCaption = FormattedBody(MessageFormat.HTML, "formatted"),
                                    source = MediaSource("url"),
                                    info = ImageInfo(
                                        height = 10L,
                                        width = 5L,
                                        mimetype = MimeTypes.Jpeg,
                                        size = 888L,
                                        thumbnailInfo = ThumbnailInfo(
                                            height = 10L,
                                            width = 5L,
                                            mimetype = MimeTypes.Jpeg,
                                            size = 111L,
                                        ),
                                        thumbnailSource = MediaSource("url_thumbnail"),
                                        blurhash = A_BLUR_HASH,
                                    )
                                )
                            )
                        ),
                    )
                )
            )
            assertThat(awaitItem()).isEqualTo(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(
                            MediaItem.Image(
                                id = A_UNIQUE_ID,
                                eventId = AN_EVENT_ID,
                                mediaInfo = MediaInfo(
                                    filename = "body.jpg",
                                    fileSize = 888L,
                                    caption = "body.jpg caption",
                                    formattedCaption = "formatted",
                                    mimeType = MimeTypes.Jpeg,
                                    formattedFileSize = "888 Bytes",
                                    fileExtension = "jpg",
                                    senderId = A_USER_ID,
                                    senderName = "alice",
                                    senderAvatar = null,
                                    dateSent = "0 Day false",
                                    dateSentFull = "0 Full false",
                                    waveform = null,
                                    duration = null
                                ),
                                mediaSource = MediaSource("url"),
                                thumbnailSource = MediaSource("url_thumbnail"),
                                blurHash = A_BLUR_HASH,
                                validationState = noopValidationState,
                            )
                        ),
                        fileItems = persistentListOf()
                    )
                )
            )
        }
    }
}

private val noopValidationState = NoopContentValidationState()

internal fun TestScope.createTimelineMediaGalleryDataSource(
    room: JoinedRoom = FakeJoinedRoom(
        liveTimeline = FakeTimeline(),
    ),
    mediaTimeline: MediaTimeline? = null,
): TimelineMediaGalleryDataSource {
    return TimelineMediaGalleryDataSource(
        room = room,
        mediaTimeline = mediaTimeline ?: LiveMediaTimeline(room),
        timelineMediaItemsFactory = createTimelineMediaItemsFactory(),
        mediaItemsPostProcessor = MediaItemsPostProcessor(),
    )
}

fun TestScope.createTimelineMediaItemsFactory() = TimelineMediaItemsFactory(
    dispatchers = testCoroutineDispatchers(),
    virtualItemFactory = VirtualItemFactory(
        dateFormatter = FakeDateFormatter(),
    ),
    eventItemFactory = EventItemFactory(
        fileSizeFormatter = FakeFileSizeFormatter(),
        fileExtensionExtractor = FileExtensionExtractorWithoutValidation(),
        dateFormatter = FakeDateFormatter(),
        contentValidationCache = NoopEventContentValidationCache(noopValidationState),
    )
)
