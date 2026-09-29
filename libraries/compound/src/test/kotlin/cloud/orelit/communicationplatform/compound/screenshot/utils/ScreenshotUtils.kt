/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.compound.screenshot.utils

import java.io.File

/**
 * Returns a [File] object for a screenshot with the given [filename].
 * This is to ensure we have a consistent location for all screenshots.
 */
fun screenshotFile(filename: String): File {
    return File("screenshots", filename)
}
