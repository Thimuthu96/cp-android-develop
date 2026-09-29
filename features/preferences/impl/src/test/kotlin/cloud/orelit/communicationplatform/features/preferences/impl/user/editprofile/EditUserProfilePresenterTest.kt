/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.preferences.impl.user.editprofile

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import cloud.orelit.communicationplatform.libraries.androidutils.file.TemporaryUriDeleter
import cloud.orelit.communicationplatform.libraries.architecture.AsyncAction
import cloud.orelit.communicationplatform.libraries.matrix.api.MatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.test.AN_AVATAR_URL
import cloud.orelit.communicationplatform.libraries.matrix.test.A_USER_ID
import cloud.orelit.communicationplatform.libraries.matrix.test.FakeMatrixClient
import cloud.orelit.communicationplatform.libraries.matrix.ui.components.aMatrixUser
import cloud.orelit.communicationplatform.libraries.matrix.ui.media.AvatarAction
import cloud.orelit.communicationplatform.libraries.mediapickers.test.FakePickerProvider
import cloud.orelit.communicationplatform.libraries.mediaupload.api.MediaUploadInfo
import cloud.orelit.communicationplatform.libraries.mediaupload.test.FakeMediaOptimizationConfigProvider
import cloud.orelit.communicationplatform.libraries.mediaupload.test.FakeMediaPreProcessor
import cloud.orelit.communicationplatform.libraries.permissions.api.PermissionsPresenter
import cloud.orelit.communicationplatform.libraries.permissions.test.FakePermissionsPresenter
import cloud.orelit.communicationplatform.libraries.permissions.test.FakePermissionsPresenterFactory
import cloud.orelit.communicationplatform.tests.testutils.WarmUpRule
import cloud.orelit.communicationplatform.tests.testutils.consumeItemsUntilPredicate
import cloud.orelit.communicationplatform.tests.testutils.consumeItemsUntilTimeout
import cloud.orelit.communicationplatform.tests.testutils.fake.FakeTemporaryUriDeleter
import cloud.orelit.communicationplatform.tests.testutils.lambda.lambdaRecorder
import cloud.orelit.communicationplatform.tests.testutils.lambda.value
import cloud.orelit.communicationplatform.tests.testutils.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@ExperimentalCoroutinesApi
class EditUserProfilePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private lateinit var fakePickerProvider: FakePickerProvider
    private lateinit var fakeMediaPreProcessor: FakeMediaPreProcessor

    private val userAvatarUri: Uri = mockk()
    private val anotherAvatarUri: Uri = mockk()

    @Before
    fun setup() {
        fakePickerProvider = FakePickerProvider()
        fakeMediaPreProcessor = FakeMediaPreProcessor()
        mockkStatic(Uri::class)

        every { Uri.parse(AN_AVATAR_URL) } returns userAvatarUri
        every { userAvatarUri.toString() } returns AN_AVATAR_URL
        every { Uri.parse(ANOTHER_AVATAR_URL) } returns anotherAvatarUri
        every { anotherAvatarUri.toString() } returns ANOTHER_AVATAR_URL
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun createEditUserProfilePresenter(
        matrixClient: MatrixClient = FakeMatrixClient(),
        navigator: EditUserProfileNavigator = FakeEditUserProfileNavigator(),
        matrixUser: MatrixUser = aMatrixUser(),
        permissionsPresenter: PermissionsPresenter = FakePermissionsPresenter(),
        temporaryUriDeleter: TemporaryUriDeleter = FakeTemporaryUriDeleter(),
        mediaOptimizationConfigProvider: FakeMediaOptimizationConfigProvider = FakeMediaOptimizationConfigProvider(),
    ): EditUserProfilePresenter {
        return EditUserProfilePresenter(
            matrixClient = matrixClient,
            navigator = navigator,
            matrixUser = matrixUser,
            mediaPickerProvider = fakePickerProvider,
            mediaPreProcessor = fakeMediaPreProcessor,
            temporaryUriDeleter = temporaryUriDeleter,
            permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionsPresenter),
            mediaOptimizationConfigProvider = mediaOptimizationConfigProvider,
        )
    }

    @Test
    fun `present - initial state is created from user info`() = runTest {
        val user = aMatrixUser(avatarUrl = AN_AVATAR_URL)
        val presenter = createEditUserProfilePresenter(matrixUser = user)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.userId).isEqualTo(user.userId)
            assertThat(initialState.displayName).isEqualTo(user.displayName)
            assertThat(initialState.userAvatarUrl).isEqualTo(AN_AVATAR_URL)
            assertThat(initialState.avatarActions).containsExactly(
                AvatarAction.ChoosePhoto,
                AvatarAction.TakePhoto,
                AvatarAction.Remove
            )
            assertThat(initialState.saveButtonEnabled).isFalse()
            assertThat(initialState.saveAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
        }
    }

    @Test
    fun `present - exit invokes the expected callback`() = runTest {
        val user = aMatrixUser(avatarUrl = AN_AVATAR_URL)
        val closeLambda = lambdaRecorder<Unit> {}
        val presenter = createEditUserProfilePresenter(
            matrixUser = user,
            navigator = FakeEditUserProfileNavigator(closeLambda),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(EditUserProfileEvent.Exit)
            closeLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - exit without unsaved changes`() = runTest {
        val user = aMatrixUser(avatarUrl = AN_AVATAR_URL)
        val closeLambda = lambdaRecorder<Unit> {}
        val presenter = createEditUserProfilePresenter(
            matrixUser = user,
            navigator = FakeEditUserProfileNavigator(closeLambda),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(EditUserProfileEvent.UpdateDisplayName("New name"))
            val withUpdatedName = awaitItem()
            withUpdatedName.eventSink(EditUserProfileEvent.Exit)
            val withConfirmation = awaitItem()
            assertThat(withConfirmation.saveAction).isEqualTo(AsyncAction.ConfirmingCancellation)
            // Cancel
            withConfirmation.eventSink(EditUserProfileEvent.CloseDialog)
            val afterCancel = awaitItem()
            assertThat(afterCancel.saveAction).isEqualTo(AsyncAction.Uninitialized)
            // Try again and confirm
            afterCancel.eventSink(EditUserProfileEvent.Exit)
            val withConfirmation2 = awaitItem()
            assertThat(withConfirmation2.saveAction).isEqualTo(AsyncAction.ConfirmingCancellation)
            closeLambda.assertions().isNeverCalled()
            withConfirmation2.eventSink(EditUserProfileEvent.Exit)
            // Dialog is closed
            val finalState = awaitItem()
            assertThat(finalState.saveAction).isEqualTo(AsyncAction.Uninitialized)
            closeLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - updates state in response to changes`() = runTest {
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val presenter = createEditUserProfilePresenter(
            matrixUser = user,
            temporaryUriDeleter = FakeTemporaryUriDeleter(
                deleteLambda = { assertThat(it).isEqualTo(userAvatarUri) }
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.displayName).isEqualTo("Name")
            assertThat(initialState.userAvatarUrl).isEqualTo(AN_AVATAR_URL)
            initialState.eventSink(EditUserProfileEvent.UpdateDisplayName("Name II"))
            awaitItem().apply {
                assertThat(displayName).isEqualTo("Name II")
                assertThat(userAvatarUrl).isEqualTo(AN_AVATAR_URL)
            }
            initialState.eventSink(EditUserProfileEvent.UpdateDisplayName("Name III"))
            awaitItem().apply {
                assertThat(displayName).isEqualTo("Name III")
                assertThat(userAvatarUrl).isEqualTo(AN_AVATAR_URL)
            }
            initialState.eventSink(EditUserProfileEvent.HandleAvatarAction(AvatarAction.Remove))
            awaitItem().apply {
                assertThat(displayName).isEqualTo("Name III")
                assertThat(userAvatarUrl).isNull()
            }
        }
    }

    @Test
    fun `present - obtains avatar uris from gallery`() = runTest {
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        fakePickerProvider.givenResult(anotherAvatarUri)
        val presenter = createEditUserProfilePresenter(
            matrixUser = user,
            temporaryUriDeleter = FakeTemporaryUriDeleter(
                deleteLambda = { assertThat(it).isEqualTo(userAvatarUri) }
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.userAvatarUrl).isEqualTo(AN_AVATAR_URL)
            initialState.eventSink(EditUserProfileEvent.HandleAvatarAction(AvatarAction.ChoosePhoto))
            awaitItem().apply {
                assertThat(userAvatarUrl).isEqualTo(ANOTHER_AVATAR_URL)
            }
        }
    }

    @Test
    fun `present - obtains avatar uris from camera`() = runTest {
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        fakePickerProvider.givenResult(anotherAvatarUri)
        val fakePermissionsPresenter = FakePermissionsPresenter()
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createEditUserProfilePresenter(
            matrixUser = user,
            permissionsPresenter = fakePermissionsPresenter,
            temporaryUriDeleter = FakeTemporaryUriDeleter(
                deleteLambda = deleteCallback,
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.userAvatarUrl).isEqualTo(AN_AVATAR_URL)
            assertThat(initialState.cameraPermissionState.permissionGranted).isFalse()
            initialState.eventSink(EditUserProfileEvent.HandleAvatarAction(AvatarAction.TakePhoto))
            val stateWithAskingPermission = awaitItem()
            assertThat(stateWithAskingPermission.cameraPermissionState.showDialog).isTrue()
            fakePermissionsPresenter.setPermissionGranted()
            val stateWithPermission = awaitItem()
            assertThat(stateWithPermission.cameraPermissionState.permissionGranted).isTrue()
            val stateWithNewAvatar = awaitItem()
            assertThat(stateWithNewAvatar.userAvatarUrl).isEqualTo(ANOTHER_AVATAR_URL)
            // Do it again, no permission is requested
            fakePickerProvider.givenResult(userAvatarUri)
            stateWithNewAvatar.eventSink(EditUserProfileEvent.HandleAvatarAction(AvatarAction.TakePhoto))
            val stateWithNewAvatar2 = awaitItem()
            assertThat(stateWithNewAvatar2.userAvatarUrl).isEqualTo(AN_AVATAR_URL)
            deleteCallback.assertions().isCalledExactly(2).withSequence(
                listOf(value(userAvatarUri)),
                listOf(value(anotherAvatarUri)),
            )
        }
    }

    @Test
    fun `present - updates save button state`() = runTest {
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        fakePickerProvider.givenResult(userAvatarUri)
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createEditUserProfilePresenter(
            matrixUser = user,
            temporaryUriDeleter = FakeTemporaryUriDeleter(
                deleteLambda = deleteCallback
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.saveButtonEnabled).isFalse()
            // Once a change is made, the save button is enabled
            initialState.eventSink(EditUserProfileEvent.UpdateDisplayName("Name II"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isTrue()
            }
            // If it's reverted then the save disables again
            initialState.eventSink(EditUserProfileEvent.UpdateDisplayName("Name"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isFalse()
            }
            // Make a change...
            initialState.eventSink(EditUserProfileEvent.HandleAvatarAction(AvatarAction.Remove))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isTrue()
            }
            // Revert it...
            initialState.eventSink(EditUserProfileEvent.HandleAvatarAction(AvatarAction.ChoosePhoto))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isFalse()
            }
            deleteCallback.assertions().isCalledExactly(2).withSequence(
                listOf(value(userAvatarUri)),
                listOf(value(null)),
            )
        }
    }

    @Test
    fun `present - updates save button state when initial values are null`() = runTest {
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = null)
        fakePickerProvider.givenResult(userAvatarUri)
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createEditUserProfilePresenter(
            matrixUser = user,
            temporaryUriDeleter = FakeTemporaryUriDeleter(
                deleteLambda = deleteCallback
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.saveButtonEnabled).isFalse()
            // Once a change is made, the save button is enabled
            initialState.eventSink(EditUserProfileEvent.UpdateDisplayName("Name II"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isTrue()
            }
            // If it's reverted then the save disables again
            initialState.eventSink(EditUserProfileEvent.UpdateDisplayName("Name"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isFalse()
            }
            // Make a change...
            initialState.eventSink(EditUserProfileEvent.HandleAvatarAction(AvatarAction.ChoosePhoto))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isTrue()
            }
            // Revert it...
            initialState.eventSink(EditUserProfileEvent.HandleAvatarAction(AvatarAction.Remove))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isFalse()
            }
            deleteCallback.assertions().isCalledExactly(2).withSequence(
                listOf(value(null)),
                listOf(value(userAvatarUri)),
            )
        }
    }

    @Test
    fun `present - save changes room details if different`() = runTest {
        val matrixClient = FakeMatrixClient()
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val presenter = createEditUserProfilePresenter(
            matrixClient = matrixClient,
            matrixUser = user,
            temporaryUriDeleter = FakeTemporaryUriDeleter(
                deleteLambda = { assertThat(it).isEqualTo(userAvatarUri) }
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(EditUserProfileEvent.UpdateDisplayName("New name"))
            initialState.eventSink(EditUserProfileEvent.HandleAvatarAction(AvatarAction.Remove))
            initialState.eventSink(EditUserProfileEvent.Save)
            consumeItemsUntilPredicate { matrixClient.setDisplayNameCalled && matrixClient.removeAvatarCalled && !matrixClient.uploadAvatarCalled }
            assertThat(matrixClient.setDisplayNameCalled).isTrue()
            assertThat(matrixClient.removeAvatarCalled).isTrue()
            assertThat(matrixClient.uploadAvatarCalled).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - save does not change room details if they're the same trimmed`() = runTest {
        val matrixClient = FakeMatrixClient()
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val presenter = createEditUserProfilePresenter(
            matrixClient = matrixClient,
            matrixUser = user
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(EditUserProfileEvent.UpdateDisplayName("   Name   "))
            initialState.eventSink(EditUserProfileEvent.Save)
            consumeItemsUntilTimeout()
            assertThat(matrixClient.setDisplayNameCalled).isFalse()
            assertThat(matrixClient.uploadAvatarCalled).isFalse()
            assertThat(matrixClient.removeAvatarCalled).isFalse()
        }
    }

    @Test
    fun `present - save does not change name if it's now empty`() = runTest {
        val matrixClient = FakeMatrixClient()
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val presenter = createEditUserProfilePresenter(
            matrixClient = matrixClient,
            matrixUser = user
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(EditUserProfileEvent.UpdateDisplayName(""))
            initialState.eventSink(EditUserProfileEvent.Save)
            assertThat(matrixClient.setDisplayNameCalled).isFalse()
            assertThat(matrixClient.uploadAvatarCalled).isFalse()
            assertThat(matrixClient.removeAvatarCalled).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - save processes and sets avatar when processor returns successfully`() = runTest {
        val matrixClient = FakeMatrixClient()
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val tmpFile = givenPickerReturnsFile()
        val presenter = createEditUserProfilePresenter(
            matrixClient = matrixClient,
            matrixUser = user,
            temporaryUriDeleter = FakeTemporaryUriDeleter(
                deleteLambda = { assertThat(it).isEqualTo(userAvatarUri) }
            ),
        )
        try {
            presenter.test {
                val initialState = awaitItem()
                initialState.eventSink(EditUserProfileEvent.HandleAvatarAction(AvatarAction.ChoosePhoto))
                initialState.eventSink(EditUserProfileEvent.Save)
                consumeItemsUntilPredicate { matrixClient.uploadAvatarCalled }
                assertThat(matrixClient.uploadAvatarCalled).isTrue()
            }
        } finally {
            tmpFile.delete()
        }
    }

    @Test
    fun `present - save does not set avatar data if processor fails`() = runTest {
        val matrixClient = FakeMatrixClient()
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val presenter = createEditUserProfilePresenter(
            matrixClient = matrixClient,
            matrixUser = user,
            temporaryUriDeleter = FakeTemporaryUriDeleter(
                deleteLambda = { assertThat(it).isEqualTo(userAvatarUri) }
            ),
        )
        fakePickerProvider.givenResult(anotherAvatarUri)
        fakeMediaPreProcessor.givenResult(Result.failure(RuntimeException("Oh no")))
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(EditUserProfileEvent.HandleAvatarAction(AvatarAction.ChoosePhoto))
            initialState.eventSink(EditUserProfileEvent.Save)
            skipItems(2)
            assertThat(matrixClient.uploadAvatarCalled).isFalse()
            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Failure::class.java)
        }
    }

    @Test
    fun `present - sets save action to failure if name update fails`() = runTest {
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val matrixClient = FakeMatrixClient().apply {
            givenSetDisplayNameResult(Result.failure(RuntimeException("!")))
        }
        saveAndAssertFailure(user, matrixClient, EditUserProfileEvent.UpdateDisplayName("New name"))
    }

    @Test
    fun `present - sets save action to failure if removing avatar fails`() = runTest {
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val matrixClient = FakeMatrixClient().apply {
            givenRemoveAvatarResult(Result.failure(RuntimeException("!")))
        }
        saveAndAssertFailure(user, matrixClient, EditUserProfileEvent.HandleAvatarAction(AvatarAction.Remove))
    }

    @Test
    fun `present - sets save action to failure if setting avatar fails`() = runTest {
        val tmpFile = givenPickerReturnsFile()
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val matrixClient = FakeMatrixClient().apply {
            givenUploadAvatarResult(Result.failure(RuntimeException("!")))
        }
        try {
            saveAndAssertFailure(user, matrixClient, EditUserProfileEvent.HandleAvatarAction(AvatarAction.ChoosePhoto))
        } finally {
            tmpFile.delete()
        }
    }

    @Test
    fun `present - CloseDialog resets save action state`() = runTest {
        val tmpFile = givenPickerReturnsFile()
        val user = aMatrixUser(id = A_USER_ID.value, displayName = "Name", avatarUrl = AN_AVATAR_URL)
        val matrixClient = FakeMatrixClient().apply {
            givenSetDisplayNameResult(Result.failure(RuntimeException("!")))
        }
        val presenter = createEditUserProfilePresenter(matrixUser = user, matrixClient = matrixClient)
        try {
            presenter.test {
                val initialState = awaitItem()
                initialState.eventSink(EditUserProfileEvent.UpdateDisplayName("foo"))
                initialState.eventSink(EditUserProfileEvent.Save)
                skipItems(2)
                assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Failure::class.java)
                initialState.eventSink(EditUserProfileEvent.CloseDialog)
                assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            }
        } finally {
            tmpFile.delete()
        }
    }

    private suspend fun saveAndAssertFailure(matrixUser: MatrixUser, matrixClient: MatrixClient, event: EditUserProfileEvent) {
        val presenter = createEditUserProfilePresenter(
            matrixUser = matrixUser,
            matrixClient = matrixClient,
            temporaryUriDeleter = FakeTemporaryUriDeleter(
                deleteLambda = { assertThat(it).isEqualTo(userAvatarUri) }
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(event)
            initialState.eventSink(EditUserProfileEvent.Save)
            skipItems(1)
            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Loading::class.java)
            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Failure::class.java)
        }
    }

    private fun givenPickerReturnsFile(): File {
        val file = File.createTempFile("test", "jpg")
        fakePickerProvider.givenResult(anotherAvatarUri)
        fakeMediaPreProcessor.givenResult(
            Result.success(
                MediaUploadInfo.AnyFile(
                    file = file,
                    fileInfo = mockk(),
                )
            )
        )
        return file
    }

    companion object {
        private const val ANOTHER_AVATAR_URL = "example://camera/foo.jpg"
    }
}
