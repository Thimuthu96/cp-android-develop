/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.push.impl.notifications

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SESSION_ID
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.fixtures.aFallbackNotifiableEvent
import cloud.orelit.communicationplatform.services.toolbox.test.systemclock.FakeSystemClock
import org.junit.Test

class FallbackNotificationFactoryTest {
    @Test
    fun `create silent event`() {
        val sut = FallbackNotificationFactory(FakeSystemClock())
        val result = sut.create(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            eventId = AN_EVENT_ID,
            cause = "A cause",
            noisy = false,
        )
        assertThat(result).isEqualTo(
            aFallbackNotifiableEvent(
                description = "",
                canBeReplaced = true,
                noisy = false,
                cause = "A cause",
            )
        )
    }

    @Test
    fun `create noisy event`() {
        val sut = FallbackNotificationFactory(FakeSystemClock())
        val result = sut.create(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            eventId = AN_EVENT_ID,
            cause = "A cause",
            noisy = true,
        )
        assertThat(result).isEqualTo(
            aFallbackNotifiableEvent(
                description = "",
                canBeReplaced = true,
                noisy = true,
                cause = "A cause",
            )
        )
    }
}
