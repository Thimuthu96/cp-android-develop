import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("cloud.orelit.communicationplatform-library")
}

setupDependencyInjection()

android {
    namespace = "cloud.orelit.communicationplatform.features.networkmonitor.impl"
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    api(projects.features.networkmonitor.api)

    testCommonDependencies(libs)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.features.networkmonitor.test)
}
