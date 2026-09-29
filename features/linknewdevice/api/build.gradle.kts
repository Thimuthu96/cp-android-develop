/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("cloud.orelit.communicationplatform-library")
}

android {
    namespace = "cloud.orelit.communicationplatform.features.linknewdevice.api"
}

dependencies {
    implementation(projects.libraries.architecture)
}
