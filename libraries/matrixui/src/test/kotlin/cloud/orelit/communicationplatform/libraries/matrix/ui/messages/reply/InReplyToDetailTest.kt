/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.ui.messages.reply

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.FormattedBody
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.InReplyTo
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MembershipChange
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MessageContent
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.MessageFormat
import cloud.orelit.communicationplatform.libraries.matrix.api.timeline.item.event.TextMessageType
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.permalink.FakePermalinkParser
import cloud.orelit.communicationplatform.libraries.matrix.test.timeline.aProfileDetails
import cloud.orelit.communicationplatform.libraries.matrix.test.timeline.item.event.aRoomMembershipContent
import org.junit.Test

class InReplyToDetailTest {
    @Test
    fun `map - with a not ready InReplyTo return expected object`() {
        assertThat(
            InReplyTo.Pending(AN_EVENT_ID).map(
                permalinkParser = FakePermalinkParser()
            )
        ).isEqualTo(InReplyToDetails.Loading(AN_EVENT_ID))
        assertThat(
            InReplyTo.NotLoaded(AN_EVENT_ID).map(
                permalinkParser = FakePermalinkParser()
            )
        ).isEqualTo(InReplyToDetails.Loading(AN_EVENT_ID))
        assertThat(
            InReplyTo.Error(AN_EVENT_ID, "a message").map(
                permalinkParser = FakePermalinkParser()
            )
        ).isEqualTo(InReplyToDetails.Error(AN_EVENT_ID, "a message"))
    }

    @Test
    fun `map - with something other than a MessageContent has no textContent`() {
        val inReplyTo = InReplyTo.Ready(
            eventId = AN_EVENT_ID,
            senderId = A_USER_ID,
            senderProfile = aProfileDetails(),
            content = aRoomMembershipContent(
                userId = A_USER_ID,
                change = MembershipChange.INVITED,
            )
        )
        val inReplyToDetails = inReplyTo.map(
            permalinkParser = FakePermalinkParser()
        )
        assertThat(inReplyToDetails).isNotNull()
        assertThat((inReplyToDetails as InReplyToDetails.Ready).textContent).isNull()
    }

    @Test
    fun `map - with a message content tries to use the formatted text if exists for its textContent`() {
        val inReplyTo = InReplyTo.Ready(
            eventId = AN_EVENT_ID,
            senderId = A_USER_ID,
            senderProfile = aProfileDetails(),
            content = MessageContent(
                body = "**Hello!**",
                inReplyTo = null,
                isEdited = false,
                threadInfo = null,
                type = TextMessageType(
                    body = "**Hello!**",
                    formatted = FormattedBody(
                        format = MessageFormat.HTML,
                        body = "<p><b>Hello!</b></p>"
                    )
                )
            )
        )
        assertThat(
            (inReplyTo.map(permalinkParser = FakePermalinkParser()) as InReplyToDetails.Ready).textContent
        ).isEqualTo("Hello!")
    }

    @Test
    fun `map - with a message content and no formatted body uses body as fallback for textContent`() {
        val inReplyTo = InReplyTo.Ready(
            eventId = AN_EVENT_ID,
            senderId = A_USER_ID,
            senderProfile = aProfileDetails(),
            content = MessageContent(
                body = "**Hello!**",
                inReplyTo = null,
                isEdited = false,
                threadInfo = null,
                type = TextMessageType(
                    body = "**Hello!**",
                    formatted = null,
                )
            )
        )
        assertThat(
            (inReplyTo.map(permalinkParser = FakePermalinkParser()) as InReplyToDetails.Ready).textContent
        ).isEqualTo("**Hello!**")
    }
}
