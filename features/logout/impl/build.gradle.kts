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

android {
    namespace = "cloud.orelit.communicationplatform.features.logout.impl"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupDependencyInjection()

dependencies {
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.testtags)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.dateformatter.api)
    implementation(projects.libraries.sessionStorage.api)
    implementation(projects.libraries.workmanager.api)
    api(projects.features.logout.api)

    testCommonDependencies(libs, true)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.libraries.sessionStorage.test)
    testImplementation(projects.libraries.workmanager.test)
}
