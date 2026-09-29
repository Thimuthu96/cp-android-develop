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
    id("cloud.orelit.communicationplatform-compose-library")
}

setupDependencyInjection()

android {
    namespace = "cloud.orelit.communicationplatform.libraries.indicator.impl"
}

dependencies {
    implementation(projects.libraries.di)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.matrix.api)

    implementation(libs.coroutines.core)

    api(projects.libraries.indicator.api)

    testCommonDependencies(libs)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.libraries.matrix.test)
}
