/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.appnav.root

import cloud.orelit.communicationplatform.features.rageshake.api.crash.CrashDetectionState
import cloud.orelit.communicationplatform.features.rageshake.api.detection.RageshakeDetectionState
import cloud.orelit.communicationplatform.services.apperror.api.AppErrorState

data class RootState(
    val rageshakeDetectionState: RageshakeDetectionState,
    val crashDetectionState: CrashDetectionState,
    val errorState: AppErrorState,
)
