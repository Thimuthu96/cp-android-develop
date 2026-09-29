/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.contentscanner.impl

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.matrix.api.scanner.ContentScanner
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_EVENT_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.scanner.FakeContentScanner
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.DefaultContentValidationState
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.contentvalidation.NoopContentValidationState
import org.junit.Test

class DefaultEventContentValidationCacheTest {
    @Test
    fun `get returns NoopContentValidationState when ContentScanner is null`() {
        // Given
        val contentScanner: ContentScanner? = null
        val cache = DefaultEventContentValidationCache(contentScanner)

        // When
        val state = cache[AN_EVENT_ID]

        // Then
        assertThat(state).isInstanceOf(NoopContentValidationState::class.java)
    }

    @Test
    fun `get returns an actual DefaultContentValidationState when ContentScanner is not null`() {
        // Given
        val contentScanner: ContentScanner? = FakeContentScanner()
        val cache = DefaultEventContentValidationCache(contentScanner)

        // When
        val state = cache[AN_EVENT_ID]

        // Then
        assertThat(state).isInstanceOf(DefaultContentValidationState::class.java)
    }
}
