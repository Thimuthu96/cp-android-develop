/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.call.impl.ui

import android.Manifest
import android.app.PictureInPictureParams
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import android.webkit.PermissionRequest
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.content.IntentCompat
import androidx.core.util.Consumer
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.compound.colors.SemanticColorsLightDark
import cloud.orelit.communicationplatform.compound.theme.ForcedDarkElementTheme
import cloud.orelit.communicationplatform.features.call.api.CallData
import cloud.orelit.communicationplatform.features.call.impl.DefaultElementCallEntryPoint
import cloud.orelit.communicationplatform.features.call.impl.di.CallBindings
import cloud.orelit.communicationplatform.features.call.impl.pip.PictureInPictureEvent
import cloud.orelit.communicationplatform.features.call.impl.pip.PictureInPicturePresenter
import cloud.orelit.communicationplatform.features.call.impl.pip.PictureInPictureState
import cloud.orelit.communicationplatform.features.call.impl.pip.PipView
import cloud.orelit.communicationplatform.features.call.impl.screenshare.ScreenCaptureProvider
import cloud.orelit.communicationplatform.features.call.impl.services.CallForegroundService
import cloud.orelit.communicationplatform.features.enterprise.api.EnterpriseService
import cloud.orelit.communicationplatform.libraries.androidutils.browser.ConsoleMessageLogger
import cloud.orelit.communicationplatform.libraries.androidutils.media.setAspectRatioFromOrientation
import cloud.orelit.communicationplatform.libraries.architecture.Presenter
import cloud.orelit.communicationplatform.libraries.architecture.bindings
import cloud.orelit.communicationplatform.libraries.audio.api.AudioFocus
import cloud.orelit.communicationplatform.libraries.audio.api.AudioFocusRequester
import cloud.orelit.communicationplatform.libraries.core.extensions.runCatchingExceptions
import cloud.orelit.communicationplatform.libraries.core.log.logger.LoggerTag
import cloud.orelit.communicationplatform.libraries.core.meta.BuildMeta
import cloud.orelit.communicationplatform.libraries.designsystem.theme.ElementThemeApp
import cloud.orelit.communicationplatform.libraries.designsystem.utils.hasCompactHeightWindowSize
import cloud.orelit.communicationplatform.libraries.featureflag.api.FeatureFlagService
import cloud.orelit.communicationplatform.libraries.featureflag.api.FeatureFlags
import cloud.orelit.communicationplatform.libraries.preferences.api.store.AppPreferencesStore
import timber.log.Timber

private val loggerTag = LoggerTag("ElementCallActivity")

class ElementCallActivity :
    AppCompatActivity(),
    CallScreenNavigator,
    PipView,
    ScreenCaptureProvider {
    @Inject lateinit var presenterFactory: CallScreenPresenter.Factory
    @Inject lateinit var appPreferencesStore: AppPreferencesStore
    @Inject lateinit var featureFlagService: FeatureFlagService
    @Inject lateinit var enterpriseService: EnterpriseService
    @Inject lateinit var pictureInPicturePresenter: PictureInPicturePresenter
    @Inject lateinit var buildMeta: BuildMeta
    @Inject lateinit var audioFocus: AudioFocus
    @Inject lateinit var consoleMessageLogger: ConsoleMessageLogger

    private lateinit var presenter: Presenter<CallScreenState>

    private var requestPermissionCallback: RequestPermissionCallback? = null

    private val requestPermissionsLauncher = registerPermissionResultLauncher()

    private var screenCaptureCallback: ((MediaProjection?) -> Unit)? = null
    private val screenCaptureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ::onScreenCaptureConsentResult)
    private var screenShareServiceConnection: ServiceConnection? = null
    private var screenShareBinder: CallForegroundService.LocalBinder? = null

    private val webViewTarget = mutableStateOf<CallData?>(null)

    private var eventSink: ((CallScreenEvent) -> Unit)? = null

    private var currentPipOrientation: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindings<CallBindings>().inject(this)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }

        setCallData(intent)
        // If presenter is not created at this point, it means we have no call to display, the Activity is finishing, so return early
        if (!::presenter.isInitialized) {
            return
        }

        pictureInPicturePresenter.setPipView(this)

        Timber.d("Created ElementCallActivity with call type: ${webViewTarget.value}")

        setContent {
            val pipState = pictureInPicturePresenter.present()
            ListenToAndroidEvents(pipState)
            val colors by remember(webViewTarget.value?.sessionId) {
                enterpriseService.semanticColorsFlow(sessionId = webViewTarget.value?.sessionId)
            }.collectAsState(SemanticColorsLightDark.default)

            // When the height is compact, hide the system bars by default to maximize the space for the call, using immersive mode
            val hasCompactHeight = hasCompactHeightWindowSize()
            DisposableEffect(hasCompactHeight, pipState.isInPictureInPicture) {
                if (hasCompactHeight && !pipState.isInPictureInPicture) {
                    val window = this@ElementCallActivity.window ?: return@DisposableEffect onDispose {}
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    val systemBarInsets = WindowInsetsCompat.Type.systemBars()
                    insetsController.hide(systemBarInsets)

                    insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

                    onDispose {
                        insetsController.show(systemBarInsets)
                        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                    }
                } else {
                    onDispose {}
                }
            }

            ElementThemeApp(
                appPreferencesStore = appPreferencesStore,
                featureFlagService = featureFlagService,
                compoundLight = colors.light,
                compoundDark = colors.dark,
                buildMeta = buildMeta,
            ) {
                ForcedDarkElementTheme(
                    colors = colors,
                ) {
                    val state = presenter.present()
                    eventSink = state.eventSink
                    val isScreenShareEnabled by remember {
                        featureFlagService.isFeatureEnabledFlow(FeatureFlags.CallScreenShare)
                    }.collectAsState(initial = null)
                    LaunchedEffect(state.isCallActive) {
                        if (state.isCallActive) {
                            setCallIsActive()
                        }
                    }
                    CallScreenView(
                        state = state,
                        pipState = pipState,
                        onConsoleMessage = {
                            consoleMessageLogger.log("ElementCall", it)
                        },
                        requestPermissions = { permissions, callback ->
                            requestPermissionCallback = callback
                            requestPermissionsLauncher.launch(permissions)
                        },
                        isScreenShareEnabled = isScreenShareEnabled,
                        screenCaptureProvider = this,
                    )
                }
            }
        }
    }

    private fun setCallIsActive() {
        audioFocus.requestAudioFocus(
            requester = AudioFocusRequester.ElementCall,
            onFocusLost = {
                // If the audio focus is lost, we do not stop the call.
                Timber.tag(loggerTag.value).w("Audio focus lost")
            }
        )
        CallForegroundService.start(this)
    }

    @Composable
    private fun ListenToAndroidEvents(pipState: PictureInPictureState) {
        val pipEventSink by rememberUpdatedState(pipState.eventSink)
        DisposableEffect(Unit) {
            val listener = Runnable {
                if (requestPermissionCallback != null || screenCaptureCallback != null) {
                    Timber.tag(loggerTag.value).w("Ignoring onUserLeaveHint event because user is asked to grant permissions")
                } else {
                    pipEventSink(PictureInPictureEvent.EnterPictureInPicture)
                }
            }
            addOnUserLeaveHintListener(listener)
            onDispose {
                removeOnUserLeaveHintListener(listener)
            }
        }
        DisposableEffect(Unit) {
            val onPictureInPictureModeChangedListener = Consumer { _: PictureInPictureModeChangedInfo ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setPipParams()
                }
                pipEventSink(PictureInPictureEvent.OnPictureInPictureModeChanged(isInPictureInPictureMode))
                if (!isInPictureInPictureMode && !lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    Timber.tag(loggerTag.value).d("Exiting PiP mode: Hangup the call")
                    eventSink?.invoke(CallScreenEvent.Hangup)
                }
            }
            addOnPictureInPictureModeChangedListener(onPictureInPictureModeChangedListener)
            onDispose {
                removeOnPictureInPictureModeChangedListener(onPictureInPictureModeChangedListener)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setCallData(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        audioFocus.releaseAudioFocus()
        releaseScreenCapture()
        CallForegroundService.stop(this)
        pictureInPicturePresenter.setPipView(null)
    }

    override fun finish() {
        // Also remove the task from recents
        finishAndRemoveTask()
    }

    override fun close() {
        finish()
    }

    private fun setCallData(intent: Intent?) {
        val callData = intent?.let {
            IntentCompat.getParcelableExtra(intent, DefaultElementCallEntryPoint.EXTRA_CALL_TYPE, CallData::class.java)
        }
        val currentCallData = webViewTarget.value
        if (currentCallData == null) {
            if (callData == null) {
                Timber.tag(loggerTag.value).d("Re-opened the activity but we have no url to load or a cached one, finish the activity")
                finish()
            } else {
                Timber.tag(loggerTag.value).d("Set the call type and create the presenter")
                webViewTarget.value = callData
                presenter = presenterFactory.create(callData, this)
            }
        } else {
            if (callData == null) {
                Timber.tag(loggerTag.value).d("Coming back from notification, do nothing")
            } else if (callData != currentCallData) {
                Timber.tag(loggerTag.value).d("User starts another call, restart the Activity")
                setIntent(intent)
                recreate()
            } else {
                // Starting the same call again, should not happen, the UI is preventing this. But maybe when using external links.
                Timber.tag(loggerTag.value).d("Starting the same call again, do nothing")
            }
        }
    }

    private fun registerPermissionResultLauncher(): ActivityResultLauncher<Array<String>> {
        return registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val callback = requestPermissionCallback ?: return@registerForActivityResult
            val permissionsToGrant = mutableListOf<String>()
            permissions.forEach { (permission, granted) ->
                if (granted) {
                    val webKitPermission = when (permission) {
                        Manifest.permission.CAMERA -> PermissionRequest.RESOURCE_VIDEO_CAPTURE
                        Manifest.permission.RECORD_AUDIO -> PermissionRequest.RESOURCE_AUDIO_CAPTURE
                        else -> return@forEach
                    }
                    permissionsToGrant.add(webKitPermission)
                }
            }
            callback(permissionsToGrant.toTypedArray())
            requestPermissionCallback = null
        }
    }

    override fun requestScreenCapture(onResult: (MediaProjection?) -> Unit) {
        if (screenCaptureCallback != null || screenShareServiceConnection != null) {
            Timber.tag(loggerTag.value).w("Screen capture already requested")
            onResult(null)
            return
        }
        screenCaptureCallback = onResult
        try {
            val mediaProjectionManager = getSystemService(MediaProjectionManager::class.java)
            screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
        } catch (e: Exception) {
            Timber.tag(loggerTag.value).e(e, "Unable to request screen capture")
            screenCaptureCallback = null
            onResult(null)
        }
    }

    private fun onScreenCaptureConsentResult(result: ActivityResult) {
        val callback = screenCaptureCallback ?: return
        screenCaptureCallback = null
        val data = result.data
        if (result.resultCode != RESULT_OK || data == null) {
            Timber.tag(loggerTag.value).d("Screen capture declined")
            callback(null)
            return
        }
        // Android requires a foreground service of type mediaProjection to be running before getting the MediaProjection.
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as? CallForegroundService.LocalBinder
                screenShareBinder = binder
                val mediaProjection = binder?.startScreenShare(result.resultCode, data)
                if (mediaProjection == null) {
                    releaseScreenCapture()
                }
                callback(mediaProjection)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                screenShareBinder = null
            }
        }
        screenShareServiceConnection = connection
        if (!CallForegroundService.bindForScreenShare(this, connection)) {
            Timber.tag(loggerTag.value).e("Unable to bind to the call foreground service")
            releaseScreenCapture()
            callback(null)
        }
    }

    override fun releaseScreenCapture() {
        val connection = screenShareServiceConnection ?: return
        screenShareServiceConnection = null
        screenShareBinder?.stopScreenShare()
        screenShareBinder = null
        runCatchingExceptions { unbindService(connection) }
            .onFailure { Timber.tag(loggerTag.value).w(it, "Unable to unbind from the call foreground service") }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setPipParams() {
        setPictureInPictureParams(getPictureInPictureParams())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun enterPipMode(): Boolean {
        return if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            enterPictureInPictureMode(getPictureInPictureParams())
        } else {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setPipOrientation(orientation: Int?) {
        currentPipOrientation = orientation
        setPictureInPictureParams(getPictureInPictureParams())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPictureInPictureParams(): PictureInPictureParams {
        // Portrait for calls seems more appropriate as a fallback value
        val orientation = currentPipOrientation ?: Configuration.ORIENTATION_PORTRAIT
        return PictureInPictureParams.Builder()
            .setAspectRatioFromOrientation(orientation)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setAutoEnterEnabled(true)
                }
            }
            .build()
    }

    override fun hangUp() {
        eventSink?.invoke(CallScreenEvent.Hangup)
    }
}

internal fun mapWebkitPermissions(permissions: Array<String>): List<String> {
    return permissions.mapNotNull { permission ->
        when (permission) {
            PermissionRequest.RESOURCE_AUDIO_CAPTURE -> Manifest.permission.RECORD_AUDIO
            PermissionRequest.RESOURCE_VIDEO_CAPTURE -> Manifest.permission.CAMERA
            else -> null
        }
    }
}
