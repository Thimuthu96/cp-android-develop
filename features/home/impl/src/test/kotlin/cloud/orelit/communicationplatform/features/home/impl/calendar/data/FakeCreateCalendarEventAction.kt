/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.data

import cloud.orelit.communicationplatform.features.home.impl.calendar.create.NewEventForm
import cloud.orelit.communicationplatform.libraries.matrix.api.core.RoomId

class FakeCreateCalendarEventAction(
    private val result: (NewEventForm) -> Result<CreatedCalendarEvent> = {
        Result.success(CreatedCalendarEvent(roomId = RoomId("!event:example.org")))
    },
) : CreateCalendarEventAction {
    val forms = mutableListOf<NewEventForm>()

    override suspend fun execute(form: NewEventForm): Result<CreatedCalendarEvent> {
        forms += form
        return result(form)
    }
}
