/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.licenses.impl.list

import cloud.orelit.communicationplatform.features.licenses.impl.model.DependencyLicenseItem
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import kotlinx.collections.immutable.ImmutableList

data class DependencyLicensesListState(
    val licenses: AsyncData<ImmutableList<DependencyLicenseItem>>,
    val filter: String,
    val eventSink: (DependencyLicensesListEvent) -> Unit,
)
