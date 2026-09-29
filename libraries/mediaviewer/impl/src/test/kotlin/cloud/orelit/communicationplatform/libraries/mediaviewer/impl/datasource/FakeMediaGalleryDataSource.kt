/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.datasource

import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.matrix.api.core.EventId
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.model.GroupedMediaItems
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeMediaGalleryDataSource(
    initialData: AsyncData<GroupedMediaItems> = AsyncData.Uninitialized,
    private val isReadyResult: () -> Boolean = { true },
    private val startLambda: () -> Unit = { lambdaError() },
    private val loadMoreLambda: (Timeline.PaginationDirection) -> Unit = { lambdaError() },
    private val deleteItemLambda: (EventId) -> Unit = { lambdaError() },
) : MediaGalleryDataSource {
    override fun start(coroutineScope: CoroutineScope) = startLambda()

    private val groupedMediaItemsFlow = MutableStateFlow(initialData)

    override val isReady: Boolean get() = isReadyResult()

    override fun groupedMediaItemsFlow(): Flow<AsyncData<GroupedMediaItems>> {
        return groupedMediaItemsFlow
    }

    suspend fun emitGroupedMediaItems(groupedMediaItems: AsyncData<GroupedMediaItems>) {
        groupedMediaItemsFlow.emit(groupedMediaItems)
    }

    override fun getLastData(): AsyncData<GroupedMediaItems> {
        return groupedMediaItemsFlow.replayCache.firstOrNull() ?: AsyncData.Uninitialized
    }

    override suspend fun loadMore(direction: Timeline.PaginationDirection) {
        loadMoreLambda(direction)
    }

    override suspend fun deleteItem(eventId: EventId) {
        deleteItemLambda(eventId)
    }
}
