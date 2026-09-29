/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.rageshake.impl.screenshot

import android.content.Context
import android.graphics.Bitmap
import androidx.core.net.toUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import cloud.orelit.communicationplatform.libraries.androidutils.bitmap.writeBitmap
import cloud.orelit.communicationplatform.libraries.androidutils.file.safeDelete
import cloud.orelit.communicationplatform.libraries.di.annotations.ApplicationContext
import java.io.File

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultScreenshotHolder(
    @ApplicationContext private val context: Context,
) : ScreenshotHolder {
    private val file = File(context.filesDir, "screenshot.png")

    override fun writeBitmap(data: Bitmap) {
        file.writeBitmap(data, Bitmap.CompressFormat.PNG, 85)
    }

    override fun getFileUri(): String? {
        return file
            .takeIf { it.exists() && it.length() > 0 }
            ?.toUri()
            ?.toString()
    }

    override fun reset() {
        file.safeDelete()
    }
}
