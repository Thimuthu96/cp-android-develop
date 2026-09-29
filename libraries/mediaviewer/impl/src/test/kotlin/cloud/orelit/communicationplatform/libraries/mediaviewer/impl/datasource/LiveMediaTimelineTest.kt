/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.mediaviewer.impl.datasource

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.matrix.api.room.CreateTimelineParams
import cloud.orelit.communicationplatform.libraries.matrix.api.room.JoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.Timeline
import cloud.orelit.communicationplatform.libraries.matrix.test.room.FakeJoinedRoom
import cloud.orelit.communicationplatform.libraries.matrix.test.timeline.FakeTimeline
import cloud.orelit.communicationplatform.libraries.mediaviewer.impl.model.GroupedMediaItems
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LiveMediaTimelineTest {
    @Test
    fun `LiveMediaTimeline cache is always null`() = runTest {
        val sut = createLiveMediaTimeline()
        assertThat<GroupedMediaItems?>(sut.cache).isNull()
    }

    @Test
    fun `getTimeline returns the timeline provided by the room, then from cache`() = runTest {
        val createTimelineResult = lambdaRecorder<CreateTimelineParams, Result<Timeline>> {
            Result.success(FakeTimeline())
        }
        val room = FakeJoinedRoom(
            createTimelineResult = createTimelineResult,
        )
        val sut = createLiveMediaTimeline(
            room = room,
        )
        val timeline = sut.getTimeline()
        assertThat(timeline.isSuccess).isTrue()
        createTimelineResult.assertions().isCalledOnce().with(value(CreateTimelineParams.MediaOnly))
        val timeline2 = sut.getTimeline()
        assertThat(timeline2.isSuccess).isTrue()
        // No called another time
        createTimelineResult.assertions().isCalledOnce()
    }

    private fun createLiveMediaTimeline(
        room: JoinedRoom = FakeJoinedRoom(),
    ) = LiveMediaTimeline(
        room = room,
    )
}
