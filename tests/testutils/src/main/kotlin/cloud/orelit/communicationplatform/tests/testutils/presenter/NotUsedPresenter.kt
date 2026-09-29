/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.tests.testutils.presenter

import androidx.compose.runtime.Composable
import cloud.orelit.communicationplatform.libraries.architecture.Presenter

/**
 * A presenter that cannot be used, util for testing Nodes.
 */
class NotUsedPresenter<T> : Presenter<T> {
    @Composable
    override fun present(): T = error("Not used")
}
