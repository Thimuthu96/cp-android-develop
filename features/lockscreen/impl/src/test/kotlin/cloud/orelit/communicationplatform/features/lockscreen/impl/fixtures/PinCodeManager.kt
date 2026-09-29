/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.lockscreen.impl.fixtures

import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.DefaultPinCodeManager
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.PinCodeManager
import cloud.orelit.communicationplatform.features.lockscreen.impl.pin.storage.InMemoryLockScreenStore
import cloud.orelit.communicationplatform.features.lockscreen.impl.storage.LockScreenStore
import cloud.orelit.communicationplatform.libraries.cryptography.api.EncryptionDecryptionService
import cloud.orelit.communicationplatform.libraries.cryptography.impl.AESEncryptionDecryptionService
import cloud.orelit.communicationplatform.libraries.cryptography.test.SimpleSecretKeyRepository

internal fun aPinCodeManager(
    lockScreenStore: LockScreenStore = InMemoryLockScreenStore(),
    secretKeyRepository: SimpleSecretKeyRepository = SimpleSecretKeyRepository(),
    encryptionDecryptionService: EncryptionDecryptionService = AESEncryptionDecryptionService(),
): PinCodeManager {
    return DefaultPinCodeManager(secretKeyRepository, encryptionDecryptionService, lockScreenStore)
}
