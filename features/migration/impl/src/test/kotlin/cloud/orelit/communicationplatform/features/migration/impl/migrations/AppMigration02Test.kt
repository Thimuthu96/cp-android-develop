/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.migration.impl.migrations

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.preferences.test.FakeSessionPreferencesStoreFactory
import cloud.orelit.communicationplatform.libraries.preferences.test.InMemorySessionPreferencesStore
import cloud.orelit.communicationplatform.libraries.sessionstorage.test.InMemorySessionStore
import cloud.orelit.communicationplatform.libraries.sessionstorage.test.aSessionData
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AppMigration02Test {
    @Test
    fun `test migration`() = runTest {
        val sessionStore = InMemorySessionStore(
            initialList = listOf(aSessionData()),
        )
        val sessionPreferencesStore = InMemorySessionPreferencesStore(isSessionVerificationSkipped = false)
        val sessionPreferencesStoreFactory = FakeSessionPreferencesStoreFactory(
            getLambda = lambdaRecorder { _, _ -> sessionPreferencesStore },
            removeLambda = lambdaRecorder { _ -> }
        )
        val migration = AppMigration02(sessionStore = sessionStore, sessionPreferenceStoreFactory = sessionPreferencesStoreFactory)

        migration.migrate(true)

        // We got the session preferences store
        sessionPreferencesStoreFactory.getLambda.assertions().isCalledOnce()
        // We changed the settings for the skipping the session verification
        assertThat(sessionPreferencesStore.isSessionVerificationSkipped().first()).isTrue()
        // We removed the session preferences store from cache
        sessionPreferencesStoreFactory.removeLambda.assertions().isCalledOnce()
    }
}
