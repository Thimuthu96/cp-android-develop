/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.call.impl.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.viewinterop.AndroidView
import cloud.orelit.communicationplatform.features.call.impl.R
import cloud.orelit.communicationplatform.features.call.impl.pip.PictureInPictureEvent
import cloud.orelit.communicationplatform.features.call.impl.pip.PictureInPictureState
import cloud.orelit.communicationplatform.features.call.impl.pip.aPictureInPictureState
import cloud.orelit.communicationplatform.features.call.impl.screenshare.NoOpScreenCaptureProvider
import cloud.orelit.communicationplatform.features.call.impl.screenshare.ScreenCaptureProvider
import cloud.orelit.communicationplatform.features.call.impl.screenshare.WebViewScreenShareController
import cloud.orelit.communicationplatform.features.call.impl.utils.InvalidAudioDeviceReason
import cloud.orelit.communicationplatform.features.call.impl.utils.WebViewAudioManager
import cloud.orelit.communicationplatform.features.call.impl.utils.WebViewPipController
import cloud.orelit.communicationplatform.features.call.impl.utils.WebViewWidgetMessageInterceptor
import cloud.orelit.communicationplatform.libraries.architecture.AsyncData
import cloud.orelit.communicationplatform.libraries.designsystem.components.ProgressDialog
import cloud.orelit.communicationplatform.libraries.designsystem.components.dialogs.ErrorDialog
import cloud.orelit.communicationplatform.libraries.designsystem.preview.ElementPreview
import cloud.orelit.communicationplatform.libraries.designsystem.preview.PreviewsDayNight
import cloud.orelit.communicationplatform.libraries.designsystem.theme.components.Text
import cloud.orelit.communicationplatform.libraries.ui.strings.CommonStrings
import timber.log.Timber

typealias RequestPermissionCallback = (Array<String>) -> Unit

interface CallScreenNavigator {
    fun close()
}

@Composable
internal fun CallScreenView(
    state: CallScreenState,
    pipState: PictureInPictureState,
    onConsoleMessage: (ConsoleMessage) -> Unit,
    requestPermissions: (Array<String>, RequestPermissionCallback) -> Unit,
    modifier: Modifier = Modifier,
    // Null while the value is not known yet, the call is not loaded until then.
    isScreenShareEnabled: Boolean? = false,
    screenCaptureProvider: ScreenCaptureProvider = NoOpScreenCaptureProvider,
) {
    var callWebView by remember { mutableStateOf<WebView?>(null) }
    // Read by the WebView when the page starts loading, which only happens once the value is known.
    val currentIsScreenShareEnabled by rememberUpdatedState(isScreenShareEnabled == true)

    fun handleBack(fromNative: Boolean = false) {
        when (CallScreenBackPressPolicy.resolve(supportPip = pipState.supportPip, hasWebView = callWebView != null, fromNative)) {
            CallScreenBackPressAction.EnterPictureInPicture ->
                pipState.eventSink(PictureInPictureEvent.EnterPictureInPicture)
            CallScreenBackPressAction.DispatchEscapeToWebView ->
                callWebView?.dispatchEscKeyEvent()
            null -> Timber.d("Back press with unsupported pip is a no-op")
        }
    }

    BackHandler {
        handleBack(fromNative = true)
    }
    if (state.webViewError != null) {
        ErrorDialog(
            content = buildString {
                append(stringResource(CommonStrings.error_unknown))
                state.webViewError.takeIf { it.isNotEmpty() }?.let { append("\n\n").append(it) }
            },
            onSubmit = { state.eventSink(CallScreenEvent.Hangup) },
        )
    } else {
        var webViewAudioManager by remember { mutableStateOf<WebViewAudioManager?>(null) }
        var screenShareController by remember { mutableStateOf<WebViewScreenShareController?>(null) }
        val coroutineScope = rememberCoroutineScope()

        var invalidAudioDeviceReason by remember { mutableStateOf<InvalidAudioDeviceReason?>(null) }
        invalidAudioDeviceReason?.let {
            InvalidAudioDeviceDialog(invalidAudioDeviceReason = it) {
                invalidAudioDeviceReason = null
            }
        }

        CallWebView(
            modifier = modifier.consumeWindowInsets(WindowInsets.systemBars).fillMaxSize(),
            url = if (isScreenShareEnabled == null) AsyncData.Loading() else state.urlState,
            userAgent = state.userAgent,
            onPermissionsRequest = { request ->
                val androidPermissions = mapWebkitPermissions(request.resources)
                val callback: RequestPermissionCallback = { request.grant(it) }
                requestPermissions(androidPermissions.toTypedArray(), callback)
            },
            onConsoleMessage = onConsoleMessage,
            onCreateWebView = { webView ->
                callWebView = webView
                webView.addBackHandler(onBackPressed = ::handleBack)
                val screenShare = WebViewScreenShareController(
                    webView = webView,
                    isEnabled = { currentIsScreenShareEnabled },
                    screenCaptureProvider = screenCaptureProvider,
                )
                screenShareController = screenShare
                val interceptor = WebViewWidgetMessageInterceptor(
                    webView = webView,
                    onUrlLoaded = { url ->
                        webView.evaluateJavascript("controls.onBackButtonPressed = () => { backHandler.onBackPressed() }", null)
                        if (webViewAudioManager?.isInCallMode?.get() == false) {
                            Timber.d("URL $url is loaded, starting in-call audio mode")
                            webViewAudioManager?.onCallStarted()
                            pipState.eventSink(PictureInPictureEvent.OnCallStarted)
                        } else {
                            Timber.d("Can't start in-call audio mode since the app is already in it.")
                        }
                    },
                    onError = { state.eventSink(CallScreenEvent.OnWebViewError(it)) },
                    interceptRequest = screenShare::interceptRequest,
                )
                webViewAudioManager = WebViewAudioManager(
                    webView = webView,
                    coroutineScope = coroutineScope,
                    onInvalidAudioDeviceAdded = { invalidAudioDeviceReason = it },
                )
                state.eventSink(CallScreenEvent.SetupMessageChannels(interceptor))
                val pipController = WebViewPipController(
                    webView = webView,
                    updatePipOrientation = { orientation ->
                        pipState.eventSink(PictureInPictureEvent.SetPipOrientation(orientation))
                    }
                )
                pipState.eventSink(PictureInPictureEvent.SetPipController(pipController))
            },
            onDestroyWebView = {
                callWebView = null
                screenShareController?.dispose()
                screenShareController = null
                // Reset audio mode
                webViewAudioManager?.onCallStopped()
            }
        )
        when (state.urlState) {
            AsyncData.Uninitialized,
            is AsyncData.Loading ->
                ProgressDialog(text = stringResource(id = CommonStrings.common_please_wait))
            is AsyncData.Failure -> {
                Timber.e(state.urlState.error, "WebView failed to load URL: ${state.urlState.error.message}")
                ErrorDialog(
                    content = state.urlState.error.message.orEmpty(),
                    onSubmit = { state.eventSink(CallScreenEvent.Hangup) },
                )
            }
            is AsyncData.Success -> Unit
        }
    }
}

@Composable
private fun InvalidAudioDeviceDialog(
    invalidAudioDeviceReason: InvalidAudioDeviceReason,
    onDismiss: () -> Unit,
) {
    ErrorDialog(
        content = when (invalidAudioDeviceReason) {
            InvalidAudioDeviceReason.BT_AUDIO_DEVICE_DISABLED -> {
                stringResource(R.string.call_invalid_audio_device_bluetooth_devices_disabled)
            }
        },
        onSubmit = onDismiss,
    )
}

@Composable
private fun CallWebView(
    url: AsyncData<String>,
    userAgent: String,
    onPermissionsRequest: (PermissionRequest) -> Unit,
    onConsoleMessage: (ConsoleMessage) -> Unit,
    onCreateWebView: (WebView) -> Unit,
    onDestroyWebView: (WebView) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("WebView - can't be previewed")
        }
    } else {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                WebView(context).apply {
                    onCreateWebView(this)
                    setup(
                        userAgent = userAgent,
                        onPermissionsRequested = onPermissionsRequest,
                        onConsoleMessage = onConsoleMessage,
                    )
                }
            },
            update = { webView ->
                if (url is AsyncData.Success && webView.url != url.data) {
                    webView.loadUrl(url.data)
                }
            },
            onRelease = { webView ->
                onDestroyWebView(webView)
                webView.destroy()
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.setup(
    userAgent: String,
    onPermissionsRequested: (PermissionRequest) -> Unit,
    onConsoleMessage: (ConsoleMessage) -> Unit,
) {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    with(settings) {
        javaScriptEnabled = true
        allowContentAccess = true
        allowFileAccess = true
        domStorageEnabled = true
        mediaPlaybackRequiresUserGesture = false
        @Suppress("DEPRECATION")
        databaseEnabled = true
        loadsImagesAutomatically = true
        userAgentString = userAgent
    }

    webChromeClient = object : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest) {
            onPermissionsRequested(request)
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            onConsoleMessage(consoleMessage)
            return true
        }
    }
}

private fun WebView.addBackHandler(onBackPressed: () -> Unit) {
    addJavascriptInterface(
        JavascriptBackHandlerBridge(callback = onBackPressed),
        "backHandler"
    )
}

private fun WebView.dispatchEscKeyEvent() {
    dispatchKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ESCAPE))
    dispatchKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_ESCAPE))
}

@PreviewsDayNight
@Composable
internal fun CallScreenViewPreview(
    @PreviewParameter(CallScreenStatePreviewParam::class) state: CallScreenState,
) = ElementPreview {
    CallScreenView(
        state = state,
        pipState = aPictureInPictureState(),
        requestPermissions = { _, _ -> },
        onConsoleMessage = {},
    )
}

@PreviewsDayNight
@Composable
internal fun InvalidAudioDeviceDialogPreview() = ElementPreview {
    InvalidAudioDeviceDialog(invalidAudioDeviceReason = InvalidAudioDeviceReason.BT_AUDIO_DEVICE_DISABLED) {}
}

internal class JavascriptBackHandlerBridge(
    private val callback: () -> Unit,
) {
    @JavascriptInterface
    fun onBackPressed() {
        callback()
    }
}
