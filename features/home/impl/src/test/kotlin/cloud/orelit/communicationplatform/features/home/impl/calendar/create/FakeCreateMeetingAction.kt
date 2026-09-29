/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.calendar.create

class FakeCreateMeetingAction(
    override val isAvailable: Boolean = true,
    private val result: (NewEventForm) -> Result<Unit> = { Result.success(Unit) },
) : CreateMeetingAction {
    val forms = mutableListOf<NewEventForm>()

    override suspend fun execute(form: NewEventForm): Result<Unit> {
        forms += form
        return result(form)
    }
}
