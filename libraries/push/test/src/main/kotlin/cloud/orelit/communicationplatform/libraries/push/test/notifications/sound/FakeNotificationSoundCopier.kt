/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.push.test.notifications.sound

import cloud.orelit.communicationplatform.libraries.push.api.notifications.sound.NotificationSoundCopier
import cloud.orelit.communicationplatform.libraries.push.api.notifications.sound.NotificationSoundCopier.CopyResult
import cloud.orelit.communicationplatform.libraries.push.api.notifications.sound.NotificationSoundCopier.SoundSlot
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeNotificationSoundCopier(
    private val copyLambda: (String, SoundSlot) -> CopyResult = { _, _ -> lambdaError() },
    private val deleteStoredSoundForLambda: (SoundSlot) -> Unit = {},
) : NotificationSoundCopier {
    override suspend fun copyToAppFiles(sourceUriString: String, slot: SoundSlot): CopyResult = copyLambda(sourceUriString, slot)

    override suspend fun deleteStoredSoundFor(slot: SoundSlot) {
        deleteStoredSoundForLambda(slot)
    }
}
