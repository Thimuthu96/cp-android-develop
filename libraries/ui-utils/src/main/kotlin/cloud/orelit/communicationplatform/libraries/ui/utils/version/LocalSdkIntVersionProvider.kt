/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.ui.utils.version

import androidx.compose.runtime.staticCompositionLocalOf
import cloud.orelit.communicationplatform.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import cloud.orelit.communicationplatform.services.toolbox.impl.sdk.DefaultBuildVersionSdkIntProvider

val LocalSdkIntVersionProvider = staticCompositionLocalOf<BuildVersionSdkIntProvider> { DefaultBuildVersionSdkIntProvider() }
