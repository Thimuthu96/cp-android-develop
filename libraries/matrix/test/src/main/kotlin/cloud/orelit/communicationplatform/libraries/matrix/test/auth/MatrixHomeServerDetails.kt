/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.test.auth

import cloud.orelit.communicationplatform.libraries.matrix.api.auth.MatrixHomeServerDetails
import cloud.orelit.communicationplatform.libraries.matrix.test.A_HOMESERVER_URL

fun aMatrixHomeServerDetails(
    url: String = A_HOMESERVER_URL,
    supportsPasswordLogin: Boolean = false,
    supportsOAuthLogin: Boolean = false,
) = MatrixHomeServerDetails(
    url = url,
    supportsPasswordLogin = supportsPasswordLogin,
    supportsOAuthLogin = supportsOAuthLogin,
)
