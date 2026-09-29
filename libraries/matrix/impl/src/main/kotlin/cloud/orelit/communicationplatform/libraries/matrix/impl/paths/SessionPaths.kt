/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.paths

import cloud.orelit.communicationplatform.libraries.matrix.api.paths.SessionPaths
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.SessionData
import java.io.File

internal fun SessionData.getSessionPaths(): SessionPaths {
    return SessionPaths(
        fileDirectory = File(sessionPath),
        cacheDirectory = File(cachePath),
    )
}
