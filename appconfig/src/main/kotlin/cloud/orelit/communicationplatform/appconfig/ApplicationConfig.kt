/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.appconfig

object ApplicationConfig {
    /**
     * Application name used in the UI for string. If empty, the value is taken from the resources `R.string.app_name`.
     * Note that this value is not used for the launcher icon.
     * For Communication Platform, the value is empty, and so read from `R.string.app_name`, which depends on the build variant:
     * - "Communication Platform" for release builds;
     * - "Communication Platform dbg" for debug builds;
     * - "Communication Platform nightly" for nightly builds.
     */
    const val APPLICATION_NAME: String = ""

    /**
     * Used in the strings to reference the client.
     * Cannot be empty.
     * For Communication Platform, the value is "Communication Platform".
     */
    const val PRODUCTION_APPLICATION_NAME: String = "Communication Platform"

    /**
     * Used in the strings to reference the Desktop client, for instance the web client.
     * Cannot be empty.
     * For Communication Platform, the value is "Communication Platform". We use the same name for desktop and mobile for now.
     */
    const val DESKTOP_APPLICATION_NAME: String = "Communication Platform"
}
