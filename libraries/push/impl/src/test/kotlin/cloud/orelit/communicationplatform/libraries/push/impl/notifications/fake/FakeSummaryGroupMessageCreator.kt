/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2021-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.push.impl.notifications.fake

import android.app.Notification
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.OneShotNotification
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.RoomNotification
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.SummaryGroupMessageCreator
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.factories.NotificationAccountParams
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.fixtures.A_NOTIFICATION
import cloud.orelit.communicationplatform.tests.testutils.lambda.LambdaFourParamsRecorder
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder

class FakeSummaryGroupMessageCreator(
    var createSummaryNotificationResult: LambdaFourParamsRecorder<
        NotificationAccountParams, List<RoomNotification>, List<OneShotNotification>, List<OneShotNotification>, Notification> =
        lambdaRecorder { _, _, _, _ -> A_NOTIFICATION }
) : SummaryGroupMessageCreator {
    override fun createSummaryNotification(
        notificationAccountParams: NotificationAccountParams,
        roomNotifications: List<RoomNotification>,
        invitationNotifications: List<OneShotNotification>,
        simpleNotifications: List<OneShotNotification>,
    ): Notification {
        return createSummaryNotificationResult(
            notificationAccountParams,
            roomNotifications,
            invitationNotifications,
            simpleNotifications,
        )
    }
}
