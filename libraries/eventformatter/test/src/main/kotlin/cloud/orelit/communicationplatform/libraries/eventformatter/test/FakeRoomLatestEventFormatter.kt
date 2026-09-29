/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.eventformatter.test

import cloud.orelit.communicationplatform.libraries.eventformatter.api.RoomLatestEventFormatter
import cloud.orelit.communicationplatform.libraries.matrix.api.roomlist.LatestEventValue

class FakeRoomLatestEventFormatter : RoomLatestEventFormatter {
    private var result: CharSequence? = null

    var lastIsDmRoom: Boolean? = null
        private set

    override fun format(latestEvent: LatestEventValue.Local, isDmRoom: Boolean): CharSequence? {
        lastIsDmRoom = isDmRoom
        return result
    }

    override fun format(latestEvent: LatestEventValue.Remote, isDmRoom: Boolean): CharSequence? {
        lastIsDmRoom = isDmRoom
        return result
    }

    fun givenFormatResult(result: CharSequence?) {
        this.result = result
    }
}
