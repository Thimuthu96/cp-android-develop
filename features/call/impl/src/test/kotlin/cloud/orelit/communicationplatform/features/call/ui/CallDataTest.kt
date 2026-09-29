/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.call.ui

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.call.api.CallData
import cloud.orelit.communicationplatform.libraries.matrix.test.A_ROOM_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.A_SESSION_ID
import org.junit.Test

class CallDataTest {
    @Test
    fun `RoomCall stringification does not contain the URL`() {
        assertThat(CallData(A_SESSION_ID, A_ROOM_ID, false).toString())
            .isEqualTo("CallData(sessionId=$A_SESSION_ID, roomId=$A_ROOM_ID, isAudioCall=false)")
    }
}
