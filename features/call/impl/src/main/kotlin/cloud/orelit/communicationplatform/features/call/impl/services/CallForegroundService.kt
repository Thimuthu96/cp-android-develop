/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.features.call.impl.services

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import cloud.orelit.communicationplatform.features.call.impl.R
import cloud.orelit.communicationplatform.features.call.impl.ui.ElementCallActivity
import cloud.orelit.communicationplatform.libraries.core.extensions.runCatchingExceptions
import cloud.orelit.communicationplatform.libraries.designsystem.utils.CommonDrawables
import cloud.orelit.communicationplatform.libraries.push.api.notifications.ForegroundServiceType
import cloud.orelit.communicationplatform.libraries.push.api.notifications.NotificationIdProvider
import timber.log.Timber

/**
 * A foreground service that shows a notification for an ongoing call while the UI is in background.
 */
class CallForegroundService : Service() {
    companion object {
        fun start(context: Context) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(context, CallForegroundService::class.java)
                ContextCompat.startForegroundService(context, intent)
            } else {
                Timber.w("Microphone permission is not granted, cannot start the call foreground service")
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, CallForegroundService::class.java)
            context.stopService(intent)
        }

        /**
         * Start the service if needed and bind to it, to be able to use [LocalBinder.startScreenShare].
         */
        fun bindForScreenShare(context: Context, connection: ServiceConnection): Boolean {
            val intent = Intent(context, CallForegroundService::class.java)
            // Started and not only bound, so that it can stay in the foreground. The app is in the foreground at this point.
            context.startService(intent)
            return context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        private fun hasMicrophonePermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        }
    }

    inner class LocalBinder : Binder() {
        /**
         * Put the service in the `mediaProjection` foreground state, as required by Android, and return the projection.
         * Must be called after the user has granted the consent, with the result of the consent screen.
         */
        fun startScreenShare(resultCode: Int, data: Intent): MediaProjection? {
            isScreenSharing = true
            val mediaProjection = if (startForegroundWithCurrentTypes()) {
                runCatchingExceptions {
                    getSystemService(MediaProjectionManager::class.java).getMediaProjection(resultCode, data)
                }.onFailure {
                    Timber.e(it, "Failed to get the media projection")
                }.getOrNull()
            } else {
                null
            }
            if (mediaProjection == null) {
                stopScreenShare()
            }
            return mediaProjection
        }

        fun stopScreenShare() {
            if (!isScreenSharing) return
            isScreenSharing = false
            if (hasMicrophonePermission(this@CallForegroundService)) {
                startForegroundWithCurrentTypes()
            } else {
                // The service was only running for the screen share
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private val binder = LocalBinder()
    private var isScreenSharing = false

    private lateinit var notificationManagerCompat: NotificationManagerCompat
    private lateinit var notification: Notification

    override fun onCreate() {
        super.onCreate()

        notificationManagerCompat = NotificationManagerCompat.from(this)

        val foregroundServiceChannel = NotificationChannelCompat.Builder(
            "call_foreground_service_channel",
            NotificationManagerCompat.IMPORTANCE_LOW,
        ).setName(
            getString(R.string.call_foreground_service_channel_title_android).ifEmpty { "Ongoing call" }
        ).build()
        notificationManagerCompat.createNotificationChannel(foregroundServiceChannel)

        val callActivityIntent = Intent(this, ElementCallActivity::class.java)
        val pendingIntent = PendingIntentCompat.getActivity(this, 0, callActivityIntent, 0, false)
        notification = NotificationCompat.Builder(this, foregroundServiceChannel.id)
            .setSmallIcon(CommonDrawables.ic_notification)
            .setContentTitle(getString(R.string.call_foreground_service_title_android))
            .setContentText(getString(R.string.call_foreground_service_message_android))
            .setContentIntent(pendingIntent)
            .build()
        // When only started for a screen share without the microphone permission, this may fail. In that case, the foreground state
        // is set by LocalBinder.startScreenShare.
        startForegroundWithCurrentTypes()
    }

    private fun startForegroundWithCurrentTypes(): Boolean {
        val notificationId = NotificationIdProvider.getForegroundServiceNotificationId(ForegroundServiceType.ONGOING_CALL)
        var serviceType = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hasMicrophonePermission(this)) {
            serviceType = serviceType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && isScreenSharing) {
            serviceType = serviceType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        }
        return runCatchingExceptions {
            ServiceCompat.startForeground(this, notificationId, notification, serviceType)
        }.onFailure {
            Timber.e(it, "Failed to start ongoing call foreground service")
        }.isSuccess
    }

    override fun onDestroy() {
        super.onDestroy()

        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}
