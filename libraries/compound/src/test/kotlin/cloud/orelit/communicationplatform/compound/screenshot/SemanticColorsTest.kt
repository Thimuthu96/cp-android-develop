/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.compound.screenshot

import com.github.takahirom.roborazzi.captureRoboImage
import cloud.orelit.communicationplatform.compound.previews.CompoundSemanticColorsDark
import cloud.orelit.communicationplatform.compound.previews.CompoundSemanticColorsDarkHc
import cloud.orelit.communicationplatform.compound.previews.CompoundSemanticColorsLight
import cloud.orelit.communicationplatform.compound.previews.CompoundSemanticColorsLightHc
import cloud.orelit.communicationplatform.compound.screenshot.utils.screenshotFile
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SemanticColorsTest : RobolectricTest() {
    @Config(sdk = [35], qualifiers = "h2000dp-xhdpi")
    @Test
    fun screenshots() {
        captureRoboImage(file = screenshotFile("Compound Semantic Colors - Light.png")) {
            CompoundSemanticColorsLight()
        }

        captureRoboImage(file = screenshotFile("Compound Semantic Colors - Light HC.png")) {
            CompoundSemanticColorsLightHc()
        }

        captureRoboImage(file = screenshotFile("Compound Semantic Colors - Dark.png")) {
            CompoundSemanticColorsDark()
        }

        captureRoboImage(file = screenshotFile("Compound Semantic Colors - Dark HC.png")) {
            CompoundSemanticColorsDarkHc()
        }
    }
}
