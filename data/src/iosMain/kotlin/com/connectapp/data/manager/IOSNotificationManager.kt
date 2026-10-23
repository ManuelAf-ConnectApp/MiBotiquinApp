package com.connectapp.data.manager

import com.connectapp.domain.manager.NotificationManager
import com.connectapp.domain.model.Medication
import com.connectapp.domain.reminder.DoseSchedule
import kotlinx.datetime.TimeZone
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

class IOSNotificationManager : NotificationManager {

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

    override fun scheduleReminders(medications: List<Medication>) {
        val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
        val timeZone = TimeZone.currentSystemDefault()
        val nowMillis = (NSDate().timeIntervalSince1970() * 1000).toLong()

        // iOS sólo conserva las 64 notificaciones pendientes más próximas de cada app y descarta
        // el resto en silencio. Se reparten entre todos los medicamentos quedándose con los
        // avisos más cercanos; la lista se rellena cada vez que se sincroniza (al abrir la app,
        // al guardar o borrar un medicamento, etc.).
        val reminders = medications
            .flatMap { medication ->
                DoseSchedule.upcomingReminders(medication, nowMillis, timeZone, MAX_PENDING_REMINDERS)
                    .map { triggerAtMillis -> medication to triggerAtMillis }
            }
            .sortedBy { (_, triggerAtMillis) -> triggerAtMillis }
            .take(MAX_PENDING_REMINDERS)

        // Se sustituye el conjunto completo: así desaparecen los avisos de medicamentos
        // borrados, editados o de otro usuario.
        notificationCenter.removeAllPendingNotificationRequests()

        reminders.forEach { (medication, triggerAtMillis) ->
            val (notifTitle, notifBody) = getNotificationTitleAndBody(medication.name)
            val content = UNMutableNotificationContent().apply {
                setTitle(notifTitle)
                setBody(notifBody)
                setSound(UNNotificationSound.defaultSound())
            }
            val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
                (triggerAtMillis - nowMillis) / 1000.0,
                repeats = false
            )
            val request = UNNotificationRequest.requestWithIdentifier(
                "med_reminder_${medication.id}_$triggerAtMillis",
                content,
                trigger
            )
            notificationCenter.addNotificationRequest(request) { error ->
                if (error != null) {
                    println("Error scheduling reminder in iOS: ${error.localizedDescription}")
                }
            }
        }
    }

    private fun getNotificationTitleAndBody(medicationName: String): Pair<String, String> {
        val preferred = platform.Foundation.NSBundle.mainBundle.preferredLocalizations
        val primaryLanguage = (preferred.firstOrNull() as? String)?.lowercase() ?: "es"
        val isSpanish = primaryLanguage.startsWith("es")

        val title = if (isSpanish) "Hora de tu medicación" else "Time for your medication"
        val body = if (isSpanish) "No olvides tomar tu dosis de: $medicationName" else "Don't forget to take your dose of: $medicationName"
        return Pair(title, body)
    }
}