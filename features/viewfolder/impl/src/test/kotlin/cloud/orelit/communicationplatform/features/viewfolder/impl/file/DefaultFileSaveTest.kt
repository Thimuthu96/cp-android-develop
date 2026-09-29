/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.viewfolder.impl.file

import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.tests.testutils.fake.registerFakeMediaStoreContentProvider
import cloud.orelit.communicationplatform.tests.testutils.robolectric.RobolectricTest
import cloud.orelit.communicationplatform.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import java.io.File

class DefaultFileSaveTest : RobolectricTest() {
    @Test
    fun `save inserts the file in the Downloads collection under its own name`() = runTest {
        val mediaStore = registerFakeMediaStoreContentProvider()
        createFileSave().save(aFileToSave().path)
        assertThat(mediaStore.insertedDisplayNames).containsExactly("a-file.txt")
        assertThat(mediaStore.savedContent("a-file.txt")).isEqualTo("Some content")
    }

    @Test
    fun `save retries with a suffixed name when the name cannot be made unique`() = runTest {
        val mediaStore = registerFakeMediaStoreContentProvider().apply {
            conflictingDisplayNames = setOf("a-file.txt")
        }
        createFileSave().save(aFileToSave().path)
        assertThat(mediaStore.insertedDisplayNames).hasSize(2)
        assertThat(mediaStore.insertedDisplayNames.last()).matches("a-file_\\d+\\.txt")
        assertThat(mediaStore.savedContent(mediaStore.insertedDisplayNames.last())).isEqualTo("Some content")
    }

    @Test
    fun `save writes nothing when the destination cannot be created`() = runTest {
        val mediaStore = registerFakeMediaStoreContentProvider().apply {
            returnsNoUriOnInsert = true
        }
        createFileSave().save(aFileToSave().path)
        assertThat(mediaStore.insertedDisplayNames).containsExactly("a-file.txt")
        assertThat(mediaStore.savedFileNames()).isEmpty()
    }

    private fun aFileToSave(): File {
        return File(RuntimeEnvironment.getApplication().cacheDir, "a-file.txt").apply {
            writeText("Some content")
        }
    }

    private fun TestScope.createFileSave() = DefaultFileSave(
        context = RuntimeEnvironment.getApplication(),
        dispatchers = testCoroutineDispatchers(),
    )
}
