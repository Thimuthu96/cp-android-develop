/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.enterprise.api

import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClientBuilder
import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId

/**
 * A hook that can be used to customize the [MatrixClientBuilder] for enterprise features.
 */
fun interface ClientBuilderEnterpriseHook {
    suspend operator fun invoke(clientBuilder: MatrixClientBuilder, sessionId: SessionId): MatrixClientBuilder
}
