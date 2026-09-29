/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.pushproviders.firebase.troubleshoot

import dev.zacsweers.metro.ContributesIntoSet
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.pushproviders.firebase.FirebaseConfig
import cloud.orelit.communicationplatform.libraries.pushproviders.firebase.IsPlayServiceAvailable
import cloud.orelit.communicationplatform.libraries.pushproviders.firebase.R
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.test.NotificationTroubleshootTestDelegate
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.test.TestFilterData
import cloud.orelit.communicationplatform.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

@ContributesIntoSet(SessionScope::class)
class FirebaseAvailabilityTest(
    private val isPlayServiceAvailable: IsPlayServiceAvailable,
    private val stringProvider: StringProvider,
) : NotificationTroubleshootTest {
    override val order = 300
    private val delegate = NotificationTroubleshootTestDelegate(
        defaultName = stringProvider.getString(R.string.troubleshoot_notifications_test_firebase_availability_title),
        defaultDescription = stringProvider.getString(R.string.troubleshoot_notifications_test_firebase_availability_description),
        visibleWhenIdle = false,
        fakeDelay = NotificationTroubleshootTestDelegate.LONG_DELAY,
    )
    override val state: StateFlow<NotificationTroubleshootTestState> = delegate.state

    override fun isRelevant(data: TestFilterData): Boolean {
        return data.currentPushProviderName == FirebaseConfig.NAME
    }

    override suspend fun run(coroutineScope: CoroutineScope) {
        delegate.start()
        val result = isPlayServiceAvailable.isAvailable()
        if (result) {
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_firebase_availability_success),
                status = NotificationTroubleshootTestState.Status.Success
            )
        } else {
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_firebase_availability_failure),
                status = NotificationTroubleshootTestState.Status.Failure()
            )
        }
    }

    override suspend fun reset() = delegate.reset()
}
