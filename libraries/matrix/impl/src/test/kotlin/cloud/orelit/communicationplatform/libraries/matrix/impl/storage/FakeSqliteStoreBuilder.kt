/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.storage

import cloud.orelit.communicationplatform.libraries.androidutils.crypto.ClientSecret
import org.matrix.rustcomponents.sdk.ClientBuilder

class FakeSqliteStoreBuilder : SqliteStoreBuilder {
    override fun secret(clientSecret: ClientSecret?): SqliteStoreBuilder = this

    override fun setupClientBuilder(clientBuilder: ClientBuilder): ClientBuilder {
        return clientBuilder
    }
}
