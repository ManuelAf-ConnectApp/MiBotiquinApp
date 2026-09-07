package com.connectapp.data.manager

import com.connectapp.domain.manager.NotificationManager
import com.connectapp.domain.model.Medication
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
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

    override fun scheduleNotification(medication: Medication) {
        val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

        val intervalMillis = medication.intervalHours * 3600_000L
        if (intervalMillis <= 0) return // guards against a degenerate/zero interval looping forever

        val timeZone = TimeZone.currentSystemDefault()
        val nowMillis = (NSDate().timeIntervalSince1970() * 1000).toLong()

        // Same precise calculation as Android: the calendar day of "startDate" combined with
        // the explicitly chosen hour/minute, minus the reminder lead time. Using the raw
        // startDate millis directly (as before) ignored startTimeHour/startTimeMinute and
        // reminderMinutes entirely, so reminders fired at the wrong time (midnight UTC of the
        // start day) instead of the time the user actually picked.
        val baseDateMillis = medication.startDate ?: nowMillis
        val localDate = Instant.fromEpochMilliseconds(baseDateMillis).toLocalDateTime(timeZone)
        val preciseDateTime = LocalDateTime(
            year = localDate.year,
            monthNumber = localDate.monthNumber,
            dayOfMonth = localDate.dayOfMonth,
            hour = medication.startTimeHour,
            minute = medication.startTimeMinute,
            second = 0,
            nanosecond = 0
        )
        var doseTimeMillis = preciseDateTime.toInstant(timeZone).toEpochMilliseconds() -
            (medication.reminderMinutes * 60_000L)

        // The UI never forces the user to pick an "endDate" (isSaveButtonEnabled doesn't
        // require it), so it's routinely null. Treating a missing endDate as "no window" made
        // the loop below never execute, meaning NO notification was ever scheduled. Fall back
        // to the duration (in days) the user did enter, like Android's receiver does.
        val endMillis = medication.endDate
            ?: (baseDateMillis + (medication.durationDays.takeIf { it > 0 } ?: 1) * 86_400_000L)

        // If the first dose already elapsed, fast-forward to the next upcoming occurrence.
        if (doseTimeMillis < nowMillis) {
            val missedDoses = (nowMillis - doseTimeMillis) / intervalMillis + 1
            doseTimeMillis += missedDoses * intervalMillis
        }

        var doseNumber = 1
        val maxIosNotifications = 64 // iOS strict local notification limit per app

        // Pre-schedule all remaining doses within the treatment window
        while (doseTimeMillis <= endMillis && doseNumber <= maxIosNotifications) {
            val secondsFromNow = (doseTimeMillis - nowMillis) / 1000.0

            if (secondsFromNow > 0) {
                val content = UNMutableNotificationContent().apply {
                    setTitle("Time for your medication")
                    setBody("Don't forget to take your dose of: ${medication.name}")
                    setSound(UNNotificationSound.defaultSound())
                }

                // Create a non-repeating trigger for this specific time gap from now
                val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
                    secondsFromNow,
                    repeats = false
                )

                // Create a composite unique identifier: e.g., "123_dose_1", "123_dose_2"
                val uniqueId = "${medication.id}_dose_$doseNumber"
                val request = UNNotificationRequest.requestWithIdentifier(uniqueId, content, trigger)

                notificationCenter.addNotificationRequest(request) { error ->
                    if (error != null) {
                        println("Error scheduling reminder in iOS: ${error.localizedDescription}")
                    }
                }
            }

            doseTimeMillis += intervalMillis
            doseNumber++
        }
    }

    override fun cancelNotification(medication: Medication) {
        val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
        
        // Cancel all dose notifications for this medication
        // We need to know how many were scheduled or just use a range
        val identifiers = (1..64).map { "${medication.id}_dose_$it" }
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(identifiers)
    }
}