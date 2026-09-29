/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package cloud.orelit.communicationplatform.libraries.push.impl.notifications

import androidx.compose.ui.graphics.toArgb
import coil3.ImageLoader
import dev.zacsweers.metro.Inject
import cloud.orelit.communicationplatform.appconfig.NotificationConfig
import cloud.orelit.communicationplatform.features.enterprise.api.EnterpriseService
import cloud.orelit.communicationplatform.libraries.core.log.logger.LoggerTag
import cloud.orelit.communicationplatform.libraries.matrix.api.user.MatrixUser
import cloud.orelit.communicationplatform.libraries.push.api.notifications.NotificationIdProvider
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.factories.NotificationAccountParams
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.factories.NotificationCreator
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.model.InviteNotifiableEvent
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.model.NotifiableEvent
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.model.NotifiableMessageEvent
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.model.NotifiableRingingCallEvent
import cloud.orelit.communicationplatform.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import cloud.orelit.communicationplatform.libraries.sessionstorage.api.SessionStore
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsLongRunningTransaction
import cloud.orelit.communicationplatform.services.analytics.api.AnalyticsService
import cloud.orelit.communicationplatform.services.analytics.api.finishLongRunningTransaction
import kotlinx.coroutines.flow.first
import timber.log.Timber

private val loggerTag = LoggerTag("NotificationRenderer", LoggerTag.NotificationLoggerTag)

@Inject
class NotificationRenderer(
    private val notificationDisplayer: NotificationDisplayer,
    private val notificationDataFactory: NotificationDataFactory,
    private val enterpriseService: EnterpriseService,
    private val sessionStore: SessionStore,
    private val analyticsService: AnalyticsService,
) {
    suspend fun render(
        currentUser: MatrixUser,
        useCompleteNotificationFormat: Boolean,
        eventsToProcess: List<NotifiableEvent>,
        imageLoader: ImageLoader,
    ) {
        val color = enterpriseService.brandColorsFlow(currentUser.userId).first()?.toArgb()
            ?: NotificationConfig.NOTIFICATION_ACCENT_COLOR
        val numberOfAccounts = sessionStore.numberOfSessions()
        val notificationAccountParams = NotificationAccountParams(
            user = currentUser,
            color = color,
            showSessionId = numberOfAccounts > 1,
        )
        val groupedEvents = eventsToProcess.groupByType()
        val roomNotifications = notificationDataFactory.toNotifications(groupedEvents.roomEvents, imageLoader, notificationAccountParams)
        val invitationNotifications = notificationDataFactory.toNotifications(groupedEvents.invitationEvents, notificationAccountParams)
        val simpleNotifications = notificationDataFactory.toNotifications(groupedEvents.simpleEvents, notificationAccountParams)
        val fallbackNotification = notificationDataFactory.toNotification(groupedEvents.fallbackEvents, notificationAccountParams)
        val summaryNotification = notificationDataFactory.createSummaryNotification(
            roomNotifications = roomNotifications,
            invitationNotifications = invitationNotifications,
            simpleNotifications = simpleNotifications,
            notificationAccountParams = notificationAccountParams,
        )

        // Remove summary first to avoid briefly displaying it after dismissing the last notification
        if (summaryNotification == SummaryNotification.Removed) {
            Timber.tag(loggerTag.value).d("Removing summary notification")
            notificationDisplayer.cancelNotification(
                tag = null,
                id = NotificationIdProvider.getSummaryNotificationId(currentUser.userId)
            )
        }

        roomNotifications.forEach { notificationData ->
            val tag = NotificationCreator.messageTag(
                roomId = notificationData.roomId,
                threadId = notificationData.threadId
            )
            notificationDisplayer.showNotification(
                tag = tag,
                id = NotificationIdProvider.getRoomMessagesNotificationId(currentUser.userId),
                notification = notificationData.notification
            )
        }

        invitationNotifications.forEach { notificationData ->
            if (useCompleteNotificationFormat) {
                Timber.tag(loggerTag.value).d("Updating invitation notification ${notificationData.tag}")
                notificationDisplayer.showNotification(
                    tag = notificationData.tag,
                    id = NotificationIdProvider.getRoomInvitationNotificationId(currentUser.userId),
                    notification = notificationData.notification
                )
            }
        }

        simpleNotifications.forEach { notificationData ->
            if (useCompleteNotificationFormat) {
                Timber.tag(loggerTag.value).d("Updating simple notification ${notificationData.tag}")
                notificationDisplayer.showNotification(
                    tag = notificationData.tag,
                    id = NotificationIdProvider.getRoomEventNotificationId(currentUser.userId),
                    notification = notificationData.notification
                )
            }
        }

        if (fallbackNotification != null) {
            Timber.tag(loggerTag.value).d("Showing or updating fallback notification")
            notificationDisplayer.showNotification(
                tag = fallbackNotification.tag,
                id = NotificationIdProvider.getFallbackNotificationId(currentUser.userId),
                notification = fallbackNotification.notification,
            )
        }

        // Update summary last to avoid briefly displaying it before other notifications
        if (summaryNotification is SummaryNotification.Update) {
            Timber.tag(loggerTag.value).d("Updating summary notification")
            notificationDisplayer.showNotification(
                tag = null,
                id = NotificationIdProvider.getSummaryNotificationId(currentUser.userId),
                notification = summaryNotification.notification
            )
        }

        for (event in eventsToProcess) {
            // Finish long-running transaction
            val uploaded = analyticsService.finishLongRunningTransaction(AnalyticsLongRunningTransaction.PushToNotification(event.eventId.value))
            Timber.d("Push-to-notification for event ${event.eventId} uploaded: $uploaded")
        }
    }
}

private fun List<NotifiableEvent>.groupByType(): GroupedNotificationEvents {
    val roomEvents: MutableList<NotifiableMessageEvent> = mutableListOf()
    val simpleEvents: MutableList<SimpleNotifiableEvent> = mutableListOf()
    val invitationEvents: MutableList<InviteNotifiableEvent> = mutableListOf()
    val fallbackEvents: MutableList<FallbackNotifiableEvent> = mutableListOf()
    forEach { event ->
        when (event) {
            is InviteNotifiableEvent -> invitationEvents.add(event.castedToEventType())
            is NotifiableMessageEvent -> roomEvents.add(event.castedToEventType())
            is SimpleNotifiableEvent -> simpleEvents.add(event.castedToEventType())
            is FallbackNotifiableEvent -> fallbackEvents.add(event.castedToEventType())
            // Nothing should be done for ringing call events as they're not handled here
            is NotifiableRingingCallEvent -> {}
        }
    }
    return GroupedNotificationEvents(roomEvents, simpleEvents, invitationEvents, fallbackEvents)
}

@Suppress("UNCHECKED_CAST")
private fun <T : NotifiableEvent> NotifiableEvent.castedToEventType(): T = this as T

data class GroupedNotificationEvents(
    val roomEvents: List<NotifiableMessageEvent>,
    val simpleEvents: List<SimpleNotifiableEvent>,
    val invitationEvents: List<InviteNotifiableEvent>,
    val fallbackEvents: List<FallbackNotifiableEvent>,
)
