package com.connectapp.data.manager

import com.connectapp.domain.manager.NotificationManager
import com.connectapp.domain.model.Medication
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

class IOSNotificationManager: NotificationManager {
    init {
        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound
        ) { granted, error ->
            if (error != null) {
                println("Error al solicitar permisos de notificación: ${error.localizedDescription}")
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

        val intervalSeconds = (medication.intervalHours * 3600).toDouble()
        val nowSeconds = NSDate().timeIntervalSince1970()
        var alarmTimeSeconds = (medication.startDate?.div(1000))?.toDouble()
        val endTimeSeconds = (medication.endDate?.div(1000))?.toDouble()

        // If the start date is in the past, calculate the first upcoming dose occurrence
        if (alarmTimeSeconds < nowSeconds) {
            val diff = nowSeconds - alarmTimeSeconds
            val pastDoses = (diff / intervalSeconds).toInt() + 1
            alarmTimeSeconds += pastDoses * intervalSeconds
        }

        var doseNumber = 1
        val maxIosNotifications = 64 // iOS strict local notification limit per app

        // Pre-schedule all remaining doses within the treatment window
        while (alarmTimeSeconds <= endTimeSeconds && doseNumber <= maxIosNotifications) {
            val secondsFromNow = alarmTimeSeconds - nowSeconds

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

            alarmTimeSeconds += intervalSeconds
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