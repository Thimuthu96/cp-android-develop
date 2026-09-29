/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.home.impl.di

import cloud.orelit.communicationplatform.features.home.impl.calendar.CalendarPresenter
import cloud.orelit.communicationplatform.features.home.impl.calendar.CalendarState
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo

@BindingContainer
@ContributesTo(SessionScope::class)
interface CalendarBindingContainer {
    @Binds
    fun bindCalendarPresenter(presenter: CalendarPresenter): Presenter<CalendarState>
}
