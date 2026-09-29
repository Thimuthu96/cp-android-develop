/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.androidutils.metadata

import cloud.orelit.communicationplatform.libraries.androidutils.BuildConfig

/**
 * true if the app is built in debug mode.
 * For testing purpose, this can be changed with [withReleaseBehavior].
 */
var isInDebug: Boolean = BuildConfig.DEBUG
    private set

/**
 * Run the lambda simulating the app is in release mode.
 *
 * **IMPORTANT**: this should **ONLY** be used for testing purposes.
 */
fun withReleaseBehavior(lambda: () -> Unit) {
    isInDebug = false
    lambda()
    isInDebug = BuildConfig.DEBUG
}
