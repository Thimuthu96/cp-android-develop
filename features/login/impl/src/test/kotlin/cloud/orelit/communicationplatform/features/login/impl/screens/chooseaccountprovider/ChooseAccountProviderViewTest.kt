/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package cloud.orelit.communicationplatform.features.login.impl.screens.chooseaccountprovider

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import cloud.orelit.communicationplatform.features.login.impl.accountprovider.anAccountProvider
import cloud.orelit.communicationplatform.features.login.impl.login.aLoginModeState
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.matrix.api.auth.OAuthDetails
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EXCEPTION
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalled
import cloud.orelit.communicationplatform.tests.testutils.EnsureNeverCalledWithParam
import cloud.orelit.communicationplatform.tests.testutils.EventsRecorder
import cloud.orelit.communicationplatform.tests.testutils.clickOn
import cloud.orelit.communicationplatform.tests.testutils.ensureCalledOnce
import cloud.orelit.communicationplatform.tests.testutils.pressBack
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.annotation.Config

class ChooseAccountProviderViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invokes the expected callback`() = runAndroidComposeUiTest {
        val eventSink = EventsRecorder<ChooseAccountProviderEvent>(expectEvents = false)
        ensureCalledOnce {
            setChooseAccountProviderView(
                state = aChooseAccountProviderState(
                    eventSink = eventSink,
                ),
                onBackClick = it,
            )
            pressBack()
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `selecting an account provider emits the the expected event`() = runAndroidComposeUiTest {
        val eventSink = EventsRecorder<ChooseAccountProviderEvent>()
        setChooseAccountProviderView(
            state = aChooseAccountProviderState(
                accountProviders = listOf(
                    ChooseAccountProviderPresenterTest.accountProvider1,
                    ChooseAccountProviderPresenterTest.accountProvider2,
                ),
                selectedAccountProvider = anAccountProvider(),
                eventSink = eventSink,
            ),
        )
        onNodeWithText(ChooseAccountProviderPresenterTest.accountProvider1.title).performClick()
        eventSink.assertSingle(ChooseAccountProviderEvent.SelectAccountProvider(ChooseAccountProviderPresenterTest.accountProvider1))
    }

    @Test
    fun `when error is displayed - closing the dialog emits the expected event`() = runAndroidComposeUiTest {
        val eventSink = EventsRecorder<ChooseAccountProviderEvent>()
        setChooseAccountProviderView(
            state = aChooseAccountProviderState(
                loginModeState = aLoginModeState(loginMode = AsyncData.Failure(AN_EXCEPTION)),
                eventSink = eventSink,
            ),
        )
        clickOn(CommonStrings.action_ok)
        eventSink.assertSingle(ChooseAccountProviderEvent.ClearError)
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setChooseAccountProviderView(
        state: ChooseAccountProviderState,
        onBackClick: () -> Unit = EnsureNeverCalled(),
        onOAuthDetails: (OAuthDetails) -> Unit = EnsureNeverCalledWithParam(),
        onNeedLoginPassword: () -> Unit = EnsureNeverCalled(),
        onLearnMoreClick: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            ChooseAccountProviderView(
                state = state,
                onBackClick = onBackClick,
                onOAuthDetails = onOAuthDetails,
                onNeedLoginPassword = onNeedLoginPassword,
                onLearnMoreClick = onLearnMoreClick,
            )
        }
    }
}
