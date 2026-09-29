/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.troubleshoot.test

import cloud.orelit.communicationplatform.libraries.troubleshoot.api.test.NotificationTroubleshootNavigator
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError

class FakeNotificationTroubleshootNavigator(
    private val openIgnoredUsersResult: () -> Unit = { lambdaError() },
) : NotificationTroubleshootNavigator {
    override fun navigateToBlockedUsers() = openIgnoredUsersResult()
}
