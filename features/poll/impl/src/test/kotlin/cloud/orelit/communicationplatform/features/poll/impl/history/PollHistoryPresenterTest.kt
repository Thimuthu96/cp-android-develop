/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.poll.impl.history

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.poll.api.actions.EndPollAction
import cloud.orelit.communicationplatform.features.poll.api.actions.SendPollResponseAction
import cloud.orelit.communicationplatform.features.poll.impl.aPollTimelineItems
import cloud.orelit.communicationplatform.features.poll.impl.anEndedPollContent
import cloud.orelit.communicationplatform.features.poll.impl.anOngoingPollContent
import cloud.orelit.communicationplatform.features.poll.impl.history.model.PollHistoryFilter
import cloud.orelit.communicationplatform.features.poll.impl.history.model.PollHistoryItemsFactory
import cloud.orelit.communicationplatform.features.poll.impl.model.DefaultPollContentStateFactory
import cloud.orelit.communicationplatform.features.poll.test.actions.FakeEndPollAction
import cloud.orelit.communicationplatform.features.poll.test.actions.FakeSendPollResponseAction
import cloud.orelit.communicationplatform.libraries.dateformatter.test.FakeDateFormatter
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID_2
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeJoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.timeline.FakeTimeline
import cloud.orelit.communicationplatform.tests.testutils.WarmUpRule
import cloud.orelit.communicationplatform.tests.testutils.lambda.assert
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class PollHistoryPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val backwardPaginationStatus = MutableStateFlow(Timeline.PaginationStatus(isPaginating = false, hasMoreToLoad = true))
    private val timeline = FakeTimeline(
        timelineItems = aPollTimelineItems(
            mapOf(
                AN_EVENT_ID to anOngoingPollContent(),
                AN_EVENT_ID_2 to anEndedPollContent()
            )
        ),
        backwardPaginationStatus = backwardPaginationStatus
    )
    private val room = FakeJoinedRoom(
        liveTimeline = timeline
    )

    @Test
    fun `present - initial states`() = runTest {
        val presenter = createPollHistoryPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().also { state ->
                assertThat(state.activeFilter).isEqualTo(PollHistoryFilter.ONGOING)
                assertThat(state.pollHistoryItems.size).isEqualTo(0)
                assertThat(state.isLoading).isTrue()
                assertThat(state.hasMoreToLoad).isTrue()
            }
            awaitItem().also { state ->
                assertThat(state.pollHistoryItems.size).isEqualTo(2)
                assertThat(state.pollHistoryItems.ongoing).hasSize(1)
                assertThat(state.pollHistoryItems.past).hasSize(1)
            }
        }
    }

    @Test
    fun `present - change filter scenario`() = runTest {
        val presenter = createPollHistoryPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().also { state ->
                assertThat(state.activeFilter).isEqualTo(PollHistoryFilter.ONGOING)
                state.eventSink(PollHistoryEvent.SelectFilter(PollHistoryFilter.PAST))
            }
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.activeFilter).isEqualTo(PollHistoryFilter.PAST)
                state.eventSink(PollHistoryEvent.SelectFilter(PollHistoryFilter.ONGOING))
            }
            awaitItem().also { state ->
                assertThat(state.activeFilter).isEqualTo(PollHistoryFilter.ONGOING)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - poll actions scenario`() = runTest {
        val sendPollResponseAction = FakeSendPollResponseAction()
        val endPollAction = FakeEndPollAction()
        val presenter = createPollHistoryPresenter(
            sendPollResponseAction = sendPollResponseAction,
            endPollAction = endPollAction
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitItem()
            state.eventSink(PollHistoryEvent.EndPoll(AN_EVENT_ID))
            runCurrent()
            endPollAction.verifyExecutionCount(1)
            state.eventSink(PollHistoryEvent.SelectPollAnswer(AN_EVENT_ID, "answer"))
            runCurrent()
            sendPollResponseAction.verifyExecutionCount(1)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `present - load more scenario`() = runTest {
        val paginateLambda = lambdaRecorder { _: Timeline.PaginationDirection ->
            Result.success(false)
        }
        timeline.apply {
            this.paginateLambda = paginateLambda
        }
        val presenter = createPollHistoryPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val loadedState = awaitItem()
            assertThat(loadedState.isLoading).isFalse()
            loadedState.eventSink(PollHistoryEvent.LoadMore)
            backwardPaginationStatus.getAndUpdate { it.copy(isPaginating = true) }
            awaitItem().also { state ->
                assertThat(state.isLoading).isTrue()
            }
            backwardPaginationStatus.getAndUpdate { it.copy(isPaginating = false) }
            awaitItem().also { state ->
                assertThat(state.isLoading).isFalse()
            }
            // Called once by the initial load and once by the load more event
            assert(paginateLambda).isCalledExactly(2)
        }
    }
}

internal fun TestScope.createPollHistoryPresenter(
    room: FakeJoinedRoom = FakeJoinedRoom(),
    endPollAction: EndPollAction = FakeEndPollAction(),
    sendPollResponseAction: SendPollResponseAction = FakeSendPollResponseAction(),
    pollHistoryItemFactory: PollHistoryItemsFactory = PollHistoryItemsFactory(
        pollContentStateFactory = DefaultPollContentStateFactory(FakeMatrixClient()),
        dateFormatter = FakeDateFormatter(),
        dispatchers = testCoroutineDispatchers(),
    ),
): PollHistoryPresenter {
    return PollHistoryPresenter(
        sessionCoroutineScope = this,
        sendPollResponseAction = sendPollResponseAction,
        endPollAction = endPollAction,
        pollHistoryItemFactory = pollHistoryItemFactory,
        room = room,
    )
}
