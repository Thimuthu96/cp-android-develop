/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.threads

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.messages.impl.fixtures.aTimelineItemContentFactory
import cloud.orelit.communicationplatform.features.messages.impl.messagesummary.FakeMessageSummaryFormatter
import cloud.orelit.communicationplatform.features.messages.impl.threads.list.ThreadsListEvent
import cloud.orelit.communicationplatform.features.messages.impl.threads.list.ThreadsListPresenter
import cloud.orelit.communicationplatform.features.messages.impl.threads.list.aThreadListItem
import cloud.orelit.communicationplatform.libraries.dateformatter.test.FakeDateFormatter
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_AVATAR_URL
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_NAME
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeJoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.room.threads.FakeThreadsListService
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ThreadsListPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        createThreadsListPresenter().test {
            awaitItem().run {
                assertThat(threads).isEmpty()
                assertThat(roomId).isEqualTo(A_ROOM_ID)
                assertThat(roomName).isEqualTo(A_ROOM_NAME)
                assertThat(roomAvatarUrl).isEqualTo(AN_AVATAR_URL)
            }
        }
    }

    @Test
    fun `present - paginate`() = runTest {
        val paginateRecorder = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val threadsListService = FakeThreadsListService(paginate = paginateRecorder)
        val room = FakeJoinedRoom(threadsListService = threadsListService)
        createThreadsListPresenter(room).test {
            val initialItem = awaitItem()

            // Pagination is automatically triggered on start, so we should have one call to paginate already
            paginateRecorder.assertions().isCalledOnce()

            initialItem.eventSink(ThreadsListEvent.Paginate)

            // Simulate a pagination result
            threadsListService.emit(listOf(aThreadListItem()))

            // We should have a second call to paginate after the event is sent
            paginateRecorder.assertions().isCalledExactly(2)

            // And we receive the new items
            assertThat(awaitItem().threads).isNotEmpty()
        }
    }

    private fun createThreadsListPresenter(
        room: FakeJoinedRoom = FakeJoinedRoom(),
    ): ThreadsListPresenter {
        return ThreadsListPresenter(
            room = room,
            timelineItemContentFactory = aTimelineItemContentFactory(),
            messageSummaryFormatter = FakeMessageSummaryFormatter(),
            dateFormatter = FakeDateFormatter(),
        )
    }
}
