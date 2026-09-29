/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.user

import cloud.orelit.communicationplatform.libraries.matrix.api.core.UserId
import cloud.orelit.communicationplatform.libraries.matrix.api.user.DisplayedStatus
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import org.matrix.rustcomponents.sdk.UserProfile

fun UserProfile.map() = MatrixUser(
    userId = UserId(userId),
    displayName = displayName,
    avatarUrl = avatarUrl,
    rawStatus = status?.into(),
    displayedStatus = DisplayedStatus.from(status, call)
)
