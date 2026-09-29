/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.call.impl.screenshare

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.concurrent.thread

class ScreenShareFrameSlotTest {
    @Test
    fun `awaitFrame returns NoFrame on timeout when nothing was published`() {
        val slot = ScreenShareFrameSlot()
        assertThat(slot.awaitFrame(timeoutMillis = 10)).isEqualTo(ScreenShareFrameSlot.Result.NoFrame)
    }

    @Test
    fun `awaitFrame returns the published frame and marks it as consumed`() {
        val slot = ScreenShareFrameSlot()
        val frame = byteArrayOf(1, 2, 3)
        slot.publish(frame)
        assertThat(slot.hasUnconsumedFrame()).isTrue()

        val result = slot.awaitFrame(timeoutMillis = 10)

        assertThat((result as ScreenShareFrameSlot.Result.Frame).bytes).isSameInstanceAs(frame)
        assertThat(slot.hasUnconsumedFrame()).isFalse()
    }

    @Test
    fun `awaitFrame only keeps the latest frame`() {
        val slot = ScreenShareFrameSlot()
        val latest = byteArrayOf(2)
        slot.publish(byteArrayOf(1))
        slot.publish(latest)

        val result = slot.awaitFrame(timeoutMillis = 10)

        assertThat((result as ScreenShareFrameSlot.Result.Frame).bytes).isSameInstanceAs(latest)
    }

    @Test
    fun `awaitFrame returns the last frame again on timeout`() {
        val slot = ScreenShareFrameSlot()
        val frame = byteArrayOf(1)
        slot.publish(frame)
        slot.awaitFrame(timeoutMillis = 10)

        val result = slot.awaitFrame(timeoutMillis = 10)

        assertThat((result as ScreenShareFrameSlot.Result.Frame).bytes).isSameInstanceAs(frame)
    }

    @Test
    fun `awaitFrame is woken up when a frame is published`() {
        val slot = ScreenShareFrameSlot()
        val frame = byteArrayOf(1)
        val publisher = thread {
            Thread.sleep(50)
            slot.publish(frame)
        }

        val result = slot.awaitFrame(timeoutMillis = 5_000)

        publisher.join()
        assertThat((result as ScreenShareFrameSlot.Result.Frame).bytes).isSameInstanceAs(frame)
    }

    @Test
    fun `awaitFrame returns Closed once closed, and publish is ignored`() {
        val slot = ScreenShareFrameSlot()
        slot.publish(byteArrayOf(1))
        slot.close()
        slot.publish(byteArrayOf(2))

        assertThat(slot.awaitFrame(timeoutMillis = 10)).isEqualTo(ScreenShareFrameSlot.Result.Closed)
    }

    @Test
    fun `close wakes up a waiting consumer`() {
        val slot = ScreenShareFrameSlot()
        val closer = thread {
            Thread.sleep(50)
            slot.close()
        }

        val result = slot.awaitFrame(timeoutMillis = 5_000)

        closer.join()
        assertThat(result).isEqualTo(ScreenShareFrameSlot.Result.Closed)
    }
}
