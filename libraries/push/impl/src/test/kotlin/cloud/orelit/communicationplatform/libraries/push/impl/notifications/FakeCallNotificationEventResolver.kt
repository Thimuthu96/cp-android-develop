/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.push.impl.notifications

import cloud.orelit.communicationplatform.libraries.matrix.api.core.SessionId
import cloud.orelit.communicationplatform.libraries.matrix.api.notification.NotificationData
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.model.NotifiableEvent
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeCallNotificationEventResolver(
    var resolveEventLambda: (sessionId: SessionId, notificationData: NotificationData, forceNotify: Boolean) -> Result<NotifiableEvent> = { _, _, _ ->
        lambdaError()
    },
) : CallNotificationEventResolver {
    override suspend fun resolveEvent(sessionId: SessionId, notificationData: NotificationData, forceNotify: Boolean): Result<NotifiableEvent> {
        return resolveEventLambda(sessionId, notificationData, forceNotify)
    }
}
