/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.sessionstorage.test.observer

import cloud.orelit.communicationplatform.libraries.sessionstorage.api.observer.SessionListener
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.observer.SessionObserver

class NoOpSessionObserver : SessionObserver {
    override fun addListener(listener: SessionListener) = Unit
    override fun removeListener(listener: SessionListener) = Unit
}
