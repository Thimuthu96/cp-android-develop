/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.call.impl.screenshare

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ComputeCaptureSizeTest {
    @Test
    fun `a portrait screen is scaled down so that its longest side is the max`() {
        assertThat(computeCaptureSize(1080, 2400, maxLongSide = 1280)).isEqualTo(CaptureSize(576, 1280))
    }

    @Test
    fun `a landscape screen is scaled down so that its longest side is the max`() {
        assertThat(computeCaptureSize(2400, 1080, maxLongSide = 1280)).isEqualTo(CaptureSize(1280, 576))
    }

    @Test
    fun `a small screen is not scaled up`() {
        assertThat(computeCaptureSize(720, 1280, maxLongSide = 1280)).isEqualTo(CaptureSize(720, 1280))
    }

    @Test
    fun `dimensions are always even`() {
        val size = computeCaptureSize(1081, 2341, maxLongSide = 1280)
        assertThat(size.width % 2).isEqualTo(0)
        assertThat(size.height % 2).isEqualTo(0)
    }

    @Test
    fun `dimensions are never smaller than 2`() {
        assertThat(computeCaptureSize(1, 10_000, maxLongSide = 1280)).isEqualTo(CaptureSize(2, 1280))
    }
}
