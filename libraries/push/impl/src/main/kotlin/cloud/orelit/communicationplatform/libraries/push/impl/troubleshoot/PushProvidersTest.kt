/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.push.impl.troubleshoot

import dev.zacsweers.metro.ContributesIntoSet
import cloud.orelit.communicationplatform.libraries.di.SessionScope
import cloud.orelit.communicationplatform.libraries.push.impl.R
import cloud.orelit.communicationplatform.libraries.pushproviders.api.PushProvider
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.test.NotificationTroubleshootTestDelegate
import cloud.orelit.communicationplatform.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import cloud.orelit.communicationplatform.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

@ContributesIntoSet(SessionScope::class)
class PushProvidersTest(
    pushProviders: Set<@JvmSuppressWildcards PushProvider>,
    private val stringProvider: StringProvider,
) : NotificationTroubleshootTest {
    private val sortedPushProvider = pushProviders.sortedBy { it.index }
    override val order = 100
    private val delegate = NotificationTroubleshootTestDelegate(
        defaultName = stringProvider.getString(R.string.troubleshoot_notifications_test_detect_push_provider_title),
        defaultDescription = stringProvider.getString(R.string.troubleshoot_notifications_test_detect_push_provider_description),
        fakeDelay = NotificationTroubleshootTestDelegate.SHORT_DELAY,
    )
    override val state: StateFlow<NotificationTroubleshootTestState> = delegate.state

    override suspend fun run(coroutineScope: CoroutineScope) {
        delegate.start()
        val result = sortedPushProvider.isNotEmpty()
        if (result) {
            delegate.updateState(
                description = stringProvider.getString(
                    resId = R.string.troubleshoot_notifications_test_detect_push_provider_success_2,
                    sortedPushProvider.joinToString { it.name }
                ),
                status = NotificationTroubleshootTestState.Status.Success
            )
        } else {
            delegate.updateState(
                description = stringProvider.getString(R.string.troubleshoot_notifications_test_detect_push_provider_failure),
                status = NotificationTroubleshootTestState.Status.Failure()
            )
        }
    }

    override suspend fun reset() = delegate.reset()
}
