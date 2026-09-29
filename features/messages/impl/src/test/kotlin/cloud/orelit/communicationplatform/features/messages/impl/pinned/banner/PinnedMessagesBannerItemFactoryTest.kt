/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.messages.impl.pinned.banner

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.eventformatter.test.FakePinnedMessagesBannerFormatter
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.MatrixTimelineItem
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.EventContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.UnknownContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_UNIQUE_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.timeline.aMessageContent
import cloud.orelit.communicationplatform.libraries.matrix.test.timeline.anEventTimelineItem
import cloud.orelit.communicationplatform.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PinnedMessagesBannerItemFactoryTest {
    @Test
    fun `create - returns an item for an event which can be displayed`() = runTest {
        val factory = createPinnedMessagesBannerItemFactory()
        val item = factory.create(anEventTimelineItemOf(aMessageContent("A message")))
        assertThat(item?.eventId).isEqualTo(AN_EVENT_ID)
    }

    @Test
    fun `create - returns null for events which cannot be displayed`() = runTest {
        val factory = createPinnedMessagesBannerItemFactory()
        val contents = listOf(
            UnknownContent,
            FailedToParseMessageLikeContent(eventType = "m.room.message", error = "an error"),
            FailedToParseStateContent(eventType = "m.room.topic", stateKey = "", error = "an error"),
        )
        for (content in contents) {
            assertThat(factory.create(anEventTimelineItemOf(content))).isNull()
        }
    }

    @Test
    fun `create - returns null for virtual items`() = runTest {
        val factory = createPinnedMessagesBannerItemFactory()
        val item = factory.create(
            MatrixTimelineItem.Virtual(
                uniqueId = A_UNIQUE_ID,
                virtual = VirtualTimelineItem.DayDivider(timestamp = 0L),
            )
        )
        assertThat(item).isNull()
    }

    private fun anEventTimelineItemOf(content: EventContent) = MatrixTimelineItem.Event(
        uniqueId = A_UNIQUE_ID,
        event = anEventTimelineItem(
            eventId = AN_EVENT_ID,
            content = content,
        ),
    )

    private fun TestScope.createPinnedMessagesBannerItemFactory() = PinnedMessagesBannerItemFactory(
        coroutineDispatchers = testCoroutineDispatchers(),
        formatter = FakePinnedMessagesBannerFormatter(
            formatLambda = { event -> "${event.content}" }
        ),
    )
}
