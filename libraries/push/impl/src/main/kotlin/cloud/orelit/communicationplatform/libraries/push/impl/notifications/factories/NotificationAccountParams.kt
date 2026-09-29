/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.push.impl.notifications.factories

import androidx.annotation.ColorInt
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser

data class NotificationAccountParams(
    val user: MatrixUser,
    @ColorInt val color: Int,
    val showSessionId: Boolean,
)
