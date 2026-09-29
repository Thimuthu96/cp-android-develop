/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.test.core

import cloud.orelit.communicationplatform.libraries.core.meta.BuildMeta
import cloud.orelit.communicationplatform.libraries.core.meta.BuildType

fun aBuildMeta(
    buildType: BuildType = BuildType.DEBUG,
    isDebuggable: Boolean = true,
    applicationName: String = "",
    productionApplicationName: String = applicationName,
    desktopApplicationName: String = applicationName,
    applicationId: String = "",
    isEnterpriseBuild: Boolean = false,
    lowPrivacyLoggingEnabled: Boolean = true,
    versionName: String = "",
    versionCode: Long = 0,
    gitRevision: String = "",
    gitBranchName: String = "",
    flavorDescription: String = "",
    flavorShortDescription: String = "",
) = BuildMeta(
    buildType = buildType,
    isDebuggable = isDebuggable,
    applicationName = applicationName,
    productionApplicationName = productionApplicationName,
    desktopApplicationName = desktopApplicationName,
    applicationId = applicationId,
    isEnterpriseBuild = isEnterpriseBuild,
    lowPrivacyLoggingEnabled = lowPrivacyLoggingEnabled,
    versionName = versionName,
    versionCode = versionCode,
    gitRevision = gitRevision,
    gitBranchName = gitBranchName,
    flavorDescription = flavorDescription,
    flavorShortDescription = flavorShortDescription,
)
