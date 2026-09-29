/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.ui.media.test

import coil3.Bitmap
import cloud.orelit.communicationplatform.libraries.designsystem.components.avatar.AvatarData
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.InitialsAvatarBitmapGenerator
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeInitialsAvatarBitmapGenerator(
    private val generateBitmapResult: (Int, AvatarData, Boolean, Float) -> Bitmap? = { _, _, _, _ -> lambdaError() }
) : InitialsAvatarBitmapGenerator {
    override fun generateBitmap(
        size: Int,
        avatarData: AvatarData,
        useDarkTheme: Boolean,
        fontSizePercentage: Float,
    ): Bitmap? {
        return generateBitmapResult(size, avatarData, useDarkTheme, fontSizePercentage)
    }
}
