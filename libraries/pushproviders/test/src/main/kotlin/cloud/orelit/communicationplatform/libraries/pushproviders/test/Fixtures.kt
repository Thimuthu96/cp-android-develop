/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.pushproviders.test

import cloud.orelit.communicationplatform.libraries.pushproviders.api.Config

fun aSessionPushConfig(
    url: String = "aUrl",
    pushKey: String = "aPushKey",
) = Config(
    url = url,
    pushKey = pushKey,
)
