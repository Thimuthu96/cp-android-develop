/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.test.search

import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId
import cloud.orelit.communicationplatform.libraries.matrix.api.search.MessageSearch
import cloud.orelit.communicationplatform.libraries.matrix.api.search.MessageSearchService
import kotlinx.coroutines.CoroutineScope

class FakeMessageSearchService(
    private val messageSearch: MessageSearch = FakeMessageSearch(),
) : MessageSearchService {
    /** The room the search was scoped to on the last call — null means all rooms. */
    var lastRoomId: RoomId? = null
        private set

    var createMessageSearchCallCount = 0
        private set

    override fun createMessageSearch(scope: CoroutineScope, roomId: RoomId?): MessageSearch {
        lastRoomId = roomId
        createMessageSearchCallCount++
        return messageSearch
    }
}
