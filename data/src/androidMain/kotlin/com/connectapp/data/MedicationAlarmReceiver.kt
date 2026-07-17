package com.connectapp.data

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class MedicationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val medId = intent.getLongExtra("MED_ID", -1L).toInt()
        val title = intent.getStringExtra("MED_TITLE") ?: "Medicación"
        val intervalMs = intent.getLongExtra("INTERVALO_MS", 0L)
        val endDateMs = intent.getLongExtra("FECHA_FIN", 0L)

        // 1. Mostrar la notificación nativa inmediatamente
        showNotification(context, medId, title)

        if (intervalMs > 0 && endDateMs > 0) {
            val nextReminderMs = System.currentTimeMillis() + intervalMs

            if (nextReminderMs <= endDateMs) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                // Reuse the existing intent extras for the next alarm in the chain
                val nextIntent = Intent(context, MedicationAlarmReceiver::class.java).apply {
                    putExtras(intent)
                }

                val flags =
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

                val pendingIntent = PendingIntent.getBroadcast(context, medId, nextIntent, flags)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            nextReminderMs,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            nextReminderMs,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextReminderMs,
                        pendingIntent
                    )
                }
            }
        }
    }

    fun showNotification(context: Context, id: Int, title: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "medication_reminders"

        val channel =
            NotificationChannel(channelId, "Recordatorios de Medicación", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Hora de tu medicación")
            .setContentText("No olvides tomar tu dosis de: $title")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(id, notification)
    }
}
