/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.rageshake.impl.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import cloud.orelit.communicationplatform.features.rageshake.api.crash.CrashDetectionPresenter
import cloud.orelit.communicationplatform.features.rageshake.api.crash.CrashDetectionState
import cloud.orelit.communicationplatform.features.rageshake.api.detection.RageshakeDetectionPresenter
import cloud.orelit.communicationplatform.features.rageshake.api.detection.RageshakeDetectionState
import cloud.orelit.communicationplatform.features.rageshake.api.preferences.RageshakePreferencesPresenter
import cloud.orelit.communicationplatform.features.rageshake.api.preferences.RageshakePreferencesState
import cloud.orelit.communicationplatform.libraries.architecture.Presenter

@ContributesTo(AppScope::class)
@BindingContainer
interface RageshakeBindingContainer {
    @Binds
    fun bindRageshakePreferencesPresenter(presenter: RageshakePreferencesPresenter): Presenter<RageshakePreferencesState>

    @Binds
    fun bindRageshakeDetectionPresenter(presenter: RageshakeDetectionPresenter): Presenter<RageshakeDetectionState>

    @Binds
    fun bindCrashDetectionPresenter(presenter: CrashDetectionPresenter): Presenter<CrashDetectionState>
}
