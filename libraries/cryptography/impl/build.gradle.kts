import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("cloud.orelit.communicationplatform-library")
}

android {
    namespace = "cloud.orelit.communicationplatform.libraries.cryptography.impl"
}

setupDependencyInjection()

dependencies {
    implementation(projects.libraries.di)
    implementation(libs.coroutines.core)
    api(projects.libraries.cryptography.api)

    testCommonDependencies(libs)
}
