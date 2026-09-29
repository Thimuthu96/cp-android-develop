/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl

import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClientBuilder
import org.matrix.rustcomponents.sdk.ClientBuilder

/**
 * A wrapper around the Matrix SDK's [ClientBuilder] to allow sharing it with other modules without exposing the SDK directly.
 */
class RustMatrixClientBuilder(
    val inner: ClientBuilder,
) : MatrixClientBuilder
