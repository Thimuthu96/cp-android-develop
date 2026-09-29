/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.login.impl.util

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import cloud.orelit.communicationplatform.appconfig.AuthenticationConfig
import cloud.orelit.communicationplatform.libraries.core.data.tryOrNull

fun openLearnMorePage(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, AuthenticationConfig.SLIDING_SYNC_READ_MORE_URL.toUri())
    tryOrNull { context.startActivity(intent) }
}
