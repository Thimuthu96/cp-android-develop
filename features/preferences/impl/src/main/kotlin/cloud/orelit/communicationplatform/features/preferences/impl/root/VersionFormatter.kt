/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.root

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import cloud.orelit.communicationplatform.libraries.core.meta.BuildMeta
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.services.toolbox.api.strings.StringProvider

interface VersionFormatter {
    fun get(): String
}

@ContributesBinding(AppScope::class)
class DefaultVersionFormatter(
    private val stringProvider: StringProvider,
    private val buildMeta: BuildMeta,
) : VersionFormatter {
    override fun get(): String {
        return stringProvider.getString(
            CommonStrings.settings_version_number,
            buildMeta.versionName,
            buildMeta.versionCode.toString()
        )
    }
}
