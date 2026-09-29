/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.matrix.impl.roomlist

import cloud.orelit.communicationplatform.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomList
import cloud.orelit.communicationplatform.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomListService
import cloud.orelit.communicationplatform.services.analytics.test.FakeAnalyticsService
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.coroutines.EmptyCoroutineContext

class RoomListFactoryTest {
    @Test
    fun `createRoomList should work`() = runTest {
        val sut = RoomListFactory(
            innerRoomListService = FakeFfiRoomListService(),
            analyticsService = FakeAnalyticsService(),
        )
        sut.createRoomList(
            pageSize = 10,
            coroutineContext = EmptyCoroutineContext,
            coroutineScope = backgroundScope,
        ) {
            FakeFfiRoomList()
        }
    }
}
