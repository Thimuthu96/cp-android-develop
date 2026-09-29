/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.push.impl.push

import cloud.orelit.communicationplatform.libraries.push.impl.db.PushRequest

fun interface SyncOnNotifiableEvent {
    suspend operator fun invoke(requests: List<PushRequest>)
}
