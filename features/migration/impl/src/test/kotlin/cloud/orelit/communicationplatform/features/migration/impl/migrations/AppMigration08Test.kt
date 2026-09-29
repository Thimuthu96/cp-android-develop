/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.migration.impl.migrations

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.features.announcement.api.Announcement
import cloud.orelit.communicationplatform.features.rageshake.test.logs.FakeAnnouncementService
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaError
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.lambda.value
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AppMigration08Test {
    @Test
    fun `migration on fresh install should not invoke the AnnouncementService`() = runTest {
        val service = FakeAnnouncementService(
            showAnnouncementResult = { lambdaError() },
        )
        val migration = AppMigration08(service)
        migration.migrate(isFreshInstall = true)
        assertThat(service.announcementsToShowFlow().first()).isEmpty()
    }

    @Test
    fun `migration on upgrade should invoke the AnnouncementService`() = runTest {
        val showAnnouncementResult = lambdaRecorder<Announcement, Unit> { }
        val service = FakeAnnouncementService(
            showAnnouncementResult = showAnnouncementResult,
        )
        val migration = AppMigration08(service)
        migration.migrate(isFreshInstall = false)
        showAnnouncementResult.assertions().isCalledOnce()
            .with(value(Announcement.NewNotificationSound))
    }
}
