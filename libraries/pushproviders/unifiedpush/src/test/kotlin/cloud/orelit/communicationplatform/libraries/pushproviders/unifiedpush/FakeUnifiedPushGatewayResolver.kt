/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.pushproviders.unifiedpush

import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeUnifiedPushGatewayResolver(
    private val getGatewayResult: (String) -> UnifiedPushGatewayResolverResult = { lambdaError() },
) : UnifiedPushGatewayResolver {
    override suspend fun getGateway(endpoint: String): UnifiedPushGatewayResolverResult {
        return getGatewayResult(endpoint)
    }
}
