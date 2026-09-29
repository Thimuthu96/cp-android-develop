/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("cloud.orelit.communicationplatform-library")
}

android {
    namespace = "cloud.orelit.communicationplatform.libraries.featureflag.test"
}

dependencies {
    api(projects.libraries.featureflag.api)
    implementation(projects.libraries.core)
    implementation(projects.libraries.matrix.test)
    implementation(libs.coroutines.core)
}
