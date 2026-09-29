/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("cloud.orelit.communicationplatform-compose-library")
}

android {
    namespace = "cloud.orelit.communicationplatform.libraries.emoji.api"
}

dependencies {
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)

    implementation(libs.kotlinx.collections.immutable)
    api(libs.matrix.emojibase.bindings)
}
