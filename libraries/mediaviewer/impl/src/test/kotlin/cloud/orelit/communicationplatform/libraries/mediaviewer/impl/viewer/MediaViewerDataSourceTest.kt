/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.viewer

import android.net.Uri
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.matrix.api.media.MatrixMediaLoader
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID_2
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EXCEPTION
import cloud.orelit.communicationplatform.libraries.matrix.test.media.FakeMatrixMediaLoader
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.NoopEventContentValidationCache
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.MediaViewerEntryPoint.MediaViewerMode
import cloud.orelit.communicationplatform.libraries.mediaviewer.api.local.LocalMediaFactory
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.datasource.FakeMediaGalleryDataSource
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.datasource.MediaGalleryDataSource
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.gallery.aGroupedMediaItems
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.model.aMediaItemDateSeparator
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.model.aMediaItemFile
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.model.aMediaItemImage
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.model.aMediaItemLoadingIndicator
import cloud.orelit.communicationplatform.libraries.mediaviewer.test.FakeLocalMediaFactory
import cloud.orelit.communicationplatform.services.toolbox.test.systemclock.A_FAKE_TIMESTAMP
import cloud.orelit.communicationplatform.services.toolbox.test.systemclock.FakeSystemClock
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.lambda.value
import cloud.orelit.communicationplatform.tests.testutils.testCoroutineDispatchers
import io.mockk.mockk
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MediaViewerDataSourceTest {
    private val mockMediaUrl: Uri = mockk("localMediaUri")

    @Test
    fun `setup should start the gallery data source`() = runTest {
        val startLambda = lambdaRecorder<Unit> { }
        val galleryDataSource = FakeMediaGalleryDataSource(
            startLambda = startLambda
        )
        val sut = createMediaViewerDataSource(
            galleryDataSource = galleryDataSource,
        )
        sut.setup(backgroundScope)
        startLambda.assertions().isCalledOnce()
    }

    @Test
    fun `test dispose`() = runTest {
        val sut = createMediaViewerDataSource()
        sut.dispose()
    }

    @Test
    fun `test dataFlow uninitialized, loading and error`() = runTest {
        val galleryDataSource = FakeMediaGalleryDataSource(
            initialData = AsyncData.Uninitialized,
        )
        val sut = createMediaViewerDataSource(
            galleryDataSource = galleryDataSource,
        )
        sut.dataFlow.test {
            // The flow starts with an empty result
            assertThat(awaitItem()).isEmpty()
            galleryDataSource.emitGroupedMediaItems(AsyncData.Uninitialized)
            assertThat(awaitItem().first()).isInstanceOf(MediaViewerPageData.Loading::class.java)
            galleryDataSource.emitGroupedMediaItems(AsyncData.Loading())
            // No items emitted, we were already loading data
            ensureAllEventsConsumed()
            galleryDataSource.emitGroupedMediaItems(AsyncData.Failure(AN_EXCEPTION))
            assertThat(awaitItem().first()).isEqualTo(MediaViewerPageData.Failure(AN_EXCEPTION))
        }
    }

    @Test
    fun `test dataFlow empty`() = runTest {
        val galleryDataSource = FakeMediaGalleryDataSource()
        val sut = createMediaViewerDataSource(
            galleryDataSource = galleryDataSource,
        )
        sut.dataFlow.test {
            galleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    aGroupedMediaItems(
                        imageAndVideoItems = listOf(),
                        fileItems = listOf(),
                    )
                )
            )
            val result = awaitItem()
            assertThat(result).isEmpty()
        }
    }

    @Test
    fun `test dataFlow loading items`() = runTest {
        val galleryDataSource = FakeMediaGalleryDataSource()
        val sut = createMediaViewerDataSource(
            galleryDataSource = galleryDataSource,
        )
        sut.dataFlow.test {
            skipItems(1)
            galleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    aGroupedMediaItems(
                        imageAndVideoItems = listOf(
                            aMediaItemLoadingIndicator(
                                direction = Timeline.PaginationDirection.BACKWARDS,
                            ),
                            aMediaItemLoadingIndicator(
                                direction = Timeline.PaginationDirection.FORWARDS,
                            ),
                        ),
                        fileItems = listOf(),
                    )
                )
            )
            val result = awaitItem()
            assertThat(result).containsExactly(
                MediaViewerPageData.Loading(
                    direction = Timeline.PaginationDirection.BACKWARDS,
                    timestamp = A_FAKE_TIMESTAMP,
                    pagerKey = 0L,
                ),
                MediaViewerPageData.Loading(
                    direction = Timeline.PaginationDirection.FORWARDS,
                    timestamp = A_FAKE_TIMESTAMP,
                    pagerKey = 1L,
                ),
            )
        }
    }

    @Test
    fun `test dataFlow with data galleryMode image`() = runTest {
        val galleryDataSource = FakeMediaGalleryDataSource()
        val sut = createMediaViewerDataSource(
            mode = MediaViewerMode.TimelineImagesAndVideos(timelineMode = Timeline.Mode.Media),
            galleryDataSource = galleryDataSource,
        )
        sut.dataFlow.test {
            skipItems(1)
            galleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    aGroupedMediaItems(
                        imageAndVideoItems = listOf(aMediaItemImage(eventId = AN_EVENT_ID)),
                        fileItems = listOf(aMediaItemFile(eventId = AN_EVENT_ID_2)),
                    )
                )
            )
            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat((result.first() as MediaViewerPageData.MediaViewerData).eventId).isEqualTo(AN_EVENT_ID)
        }
    }

    @Test
    fun `test dataFlow with data galleryMode files`() = runTest {
        val galleryDataSource = FakeMediaGalleryDataSource()
        val sut = createMediaViewerDataSource(
            mode = MediaViewerMode.TimelineFilesAndAudios(timelineMode = Timeline.Mode.Media),
            galleryDataSource = galleryDataSource,
        )
        sut.dataFlow.test {
            skipItems(1)
            galleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    aGroupedMediaItems(
                        imageAndVideoItems = listOf(aMediaItemImage(eventId = AN_EVENT_ID)),
                        fileItems = listOf(aMediaItemFile(eventId = AN_EVENT_ID_2)),
                    )
                )
            )
            val result = awaitItem()
            assertThat(result).hasSize(1)
            assertThat((result.first() as MediaViewerPageData.MediaViewerData).eventId).isEqualTo(AN_EVENT_ID_2)
        }
    }

    @Test
    fun `test dataFlow - date separator are filtered out`() = runTest {
        val galleryDataSource = FakeMediaGalleryDataSource()
        val sut = createMediaViewerDataSource(
            galleryDataSource = galleryDataSource,
        )
        sut.dataFlow.test {
            skipItems(1)
            galleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    aGroupedMediaItems(
                        imageAndVideoItems = listOf(aMediaItemDateSeparator(), aMediaItemImage(), aMediaItemDateSeparator()),
                        fileItems = emptyList(),
                    )
                )
            )
            val result = awaitItem()
            assertThat(result).hasSize(1)
        }
    }

    @Test
    fun `loadMore invokes the gallery data source loadMore`() = runTest {
        val loadMoreLambda = lambdaRecorder<Timeline.PaginationDirection, Unit> { }
        val galleryDataSource = FakeMediaGalleryDataSource(
            loadMoreLambda = loadMoreLambda
        )
        val sut = createMediaViewerDataSource(
            galleryDataSource = galleryDataSource,
        )
        sut.loadMore(Timeline.PaginationDirection.BACKWARDS)
        loadMoreLambda.assertions().isCalledOnce().with(value(Timeline.PaginationDirection.BACKWARDS))
    }

    @Test
    fun `test dataFlow with data galleryMode image and load media`() = runTest {
        val galleryDataSource = FakeMediaGalleryDataSource()
        val sut = createMediaViewerDataSource(
            galleryDataSource = galleryDataSource,
        )
        sut.dataFlow.test {
            skipItems(1)
            galleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    aGroupedMediaItems(
                        imageAndVideoItems = listOf(aMediaItemImage(eventId = AN_EVENT_ID)),
                    )
                )
            )
            val result = awaitItem()
            val mediaViewerData = result.first() as MediaViewerPageData.MediaViewerData
            assertThat(mediaViewerData.downloadedMedia.value).isEqualTo(AsyncData.Uninitialized)
            sut.loadMedia(mediaViewerData)
            assertThat(mediaViewerData.downloadedMedia.value.isSuccess()).isTrue()
        }
    }

    @Test
    fun `test dataFlow with data galleryMode image and load media with failure then success`() = runTest {
        val galleryDataSource = FakeMediaGalleryDataSource()
        val mediaLoader = FakeMatrixMediaLoader()
        val sut = createMediaViewerDataSource(
            galleryDataSource = galleryDataSource,
            mediaLoader = mediaLoader,
        )
        sut.dataFlow.test {
            skipItems(1)
            galleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    aGroupedMediaItems(
                        imageAndVideoItems = listOf(aMediaItemImage(eventId = AN_EVENT_ID)),
                    )
                )
            )
            val result = awaitItem()
            val mediaViewerData = result.first() as MediaViewerPageData.MediaViewerData
            assertThat(mediaViewerData.downloadedMedia.value).isEqualTo(AsyncData.Uninitialized)
            mediaLoader.shouldFail = true
            sut.loadMedia(mediaViewerData)
            assertThat(mediaViewerData.downloadedMedia.value.isFailure()).isTrue()
            // clear the error
            sut.clearLoadingError(mediaViewerData)
            assertThat(mediaViewerData.downloadedMedia.value).isEqualTo(AsyncData.Uninitialized)
            // load again with success
            mediaLoader.shouldFail = false
            sut.loadMedia(mediaViewerData)
            assertThat(mediaViewerData.downloadedMedia.value.isSuccess()).isTrue()
        }
    }

    private fun TestScope.createMediaViewerDataSource(
        mode: MediaViewerMode = MediaViewerMode.TimelineImagesAndVideos(timelineMode = Timeline.Mode.Media),
        galleryDataSource: MediaGalleryDataSource = FakeMediaGalleryDataSource(),
        mediaLoader: MatrixMediaLoader = FakeMatrixMediaLoader(),
        localMediaFactory: LocalMediaFactory = FakeLocalMediaFactory(mockMediaUrl),
    ) = MediaViewerDataSource(
        coroutineScope = backgroundScope,
        mode = mode,
        dispatcher = testCoroutineDispatchers().computation,
        galleryDataSource = galleryDataSource,
        mediaLoader = mediaLoader,
        localMediaFactory = localMediaFactory,
        systemClock = FakeSystemClock(),
        pagerKeysHandler = PagerKeysHandler(),
        contentScannerService = { _, _ -> },
        contentValidationCache = NoopEventContentValidationCache(),
    )
}
