package com.connectapp.data.manager

import com.connectapp.domain.manager.NotificationManager
import com.connectapp.domain.model.DoseReminder
import com.connectapp.domain.notification.ReminderNavigation
import com.connectapp.domain.model.NotificationDiagnostics
import platform.UserNotifications.UNNotificationDismissActionIdentifier
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionList
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

class IOSNotificationManager(private val navigation: ReminderNavigation) : NotificationManager {
    private var scheduledCount = 0

    private companion object {
        // Margen bajo el límite de 64 de iOS para no desplazar otras notificaciones locales.
        const val MAX_PENDING_REMINDERS = 60
    }

    // Held as a strong reference: `UNUserNotificationCenter.delegate` is a weak property,
    // so without this the delegate would be deallocated right after being assigned.
    private val notificationCenterDelegate = object : NSObject(), UNUserNotificationCenterDelegateProtocol {
        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            willPresentNotification: UNNotification,
            withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
        ) {
            // Without this delegate, iOS silently drops local notifications
            // when the app is in the foreground instead of showing them.
            withCompletionHandler(
                UNNotificationPresentationOptionBanner or
                    UNNotificationPresentationOptionSound or
                    UNNotificationPresentationOptionList
            )
        }

        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            didReceiveNotificationResponse: UNNotificationResponse,
            withCompletionHandler: () -> Unit
        ) {
            if (didReceiveNotificationResponse.actionIdentifier != UNNotificationDismissActionIdentifier) {
                val eventId = didReceiveNotificationResponse.notification.request.content.userInfo["dose_event_id"] as? String
                if (eventId != null) navigation.open(eventId)
            }
            withCompletionHandler()
        }
    }

    init {
        UNUserNotificationCenter.currentNotificationCenter().delegate = notificationCenterDelegate
        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound
        ) { granted, error ->
            if (error != null) {
                println("Error al solicitar permisos de notificación: ${error.localizedDescription}")
            } else if (!granted) {
                println("Permiso de notificaciones denegado por el usuario")
            }
        }
    }

    override fun showNotification(title: String, body: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
        }

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(1.0, false)

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "local_notif_${platform.Foundation.NSUUID.UUID().UUIDString()}",
            content = content,
            trigger = trigger
        )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
            error?.let { println("Error mostrando notificación en iOS: ${it.localizedDescription}") }
        }
    }

    override fun scheduleReminders(reminders: List<DoseReminder>) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val nowMillis = (NSDate().timeIntervalSince1970() * 1000).toLong()
        center.removeAllPendingNotificationRequests()
        val pending = reminders.filter { it.reminderAt > nowMillis }.take(MAX_PENDING_REMINDERS)
        scheduledCount = pending.size
        pending.forEach { reminder ->
            val spanish = ((platform.Foundation.NSBundle.mainBundle.preferredLocalizations.firstOrNull() as? String) ?: "es").startsWith("es")
            val content = UNMutableNotificationContent().apply {
                setTitle(if (spanish) "Recordatorio de medicación" else "Medication reminder")
                setBody(if (spanish) "Abre la app para revisar tu toma pendiente." else "Open the app to review your pending dose.")
                setSound(UNNotificationSound.defaultSound())
                setUserInfo(mapOf<Any?, Any?>("dose_event_id" to reminder.eventId))
            }
            val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
                ((reminder.reminderAt - nowMillis) / 1000.0).coerceAtLeast(1.0), false)
            center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier(reminder.eventId, content, trigger)) { _ -> }
        }
    }

    override fun diagnostics() = NotificationDiagnostics(true, null, scheduledCount, "iOS")
}
