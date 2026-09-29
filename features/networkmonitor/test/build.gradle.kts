/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("cloud.orelit.communicationplatform-library")
}

android {
    namespace = "cloud.orelit.communicationplatform.features.networkmonitor.test"
}

dependencies {
    api(projects.features.networkmonitor.api)
    api(libs.coroutines.core)
}
