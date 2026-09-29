/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.paths

import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.libraries.di.BaseDirectory
import cloud.orelit.communicationplatform.libraries.di.CacheDirectory
import cloud.orelit.communicationplatform.libraries.matrix.api.paths.SessionPaths
import java.io.File
import java.util.UUID

@Inject
class SessionPathsFactory(
    @BaseDirectory private val baseDirectory: File,
    @CacheDirectory private val cacheDirectory: File,
) {
    fun create(): SessionPaths {
        val subPath = UUID.randomUUID().toString()
        return SessionPaths(
            fileDirectory = File(baseDirectory, subPath),
            cacheDirectory = File(cacheDirectory, subPath),
        )
    }
}
