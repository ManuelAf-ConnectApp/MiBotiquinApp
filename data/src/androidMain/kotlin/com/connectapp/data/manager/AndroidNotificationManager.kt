package com.connectapp.data.manager

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.connectapp.data.MedicationAlarmReceiver
import com.connectapp.domain.manager.NotificationManager
import com.connectapp.domain.model.Medication
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import android.app.NotificationManager as AndroidNetManager

class AndroidNotificationManager(
    private val context: Context
) : NotificationManager {

    init {
        createNotificationChannel()
    }

    override fun showNotification(title: String, body: String) {
        val notification = NotificationCompat.Builder(context, "medication_reminders")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNetManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }


    private fun createNotificationChannel() {
        val locale = context.resources.configuration.locales[0]
        val isSpanish = locale.language.lowercase().startsWith("es")
        val channelName = if (isSpanish) "Recordatorios de Medicación" else "Medication Reminders"

        val channel = NotificationChannel(
            "medication_reminders",
            channelName,
            AndroidNetManager.IMPORTANCE_HIGH
        )
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNetManager
        manager.createNotificationChannel(channel)
    }


    override fun scheduleNotification(medication: Medication) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra("MED_ID", medication.id)
            putExtra("MED_TITLE", medication.name)
            val intervalMs = (24L / medication.dosesPerDay) * 3600000L
            putExtra("INTERVALO_MS", intervalMs)
            putExtra("FECHA_FIN", medication.endDate ?: 0L)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medication.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cálculo riguroso del tiempo de disparo (Fecha + Hora - Recordatorio)
        val baseDateMillis = medication.startDate ?: System.currentTimeMillis()
        val timeZone = TimeZone.currentSystemDefault()
        
        val localDateTime = Instant.fromEpochMilliseconds(baseDateMillis)
            .toLocalDateTime(timeZone)
            
        val preciseDateTime = kotlinx.datetime.LocalDateTime(
            year = localDateTime.year,
            monthNumber = localDateTime.monthNumber,
            dayOfMonth = localDateTime.dayOfMonth,
            hour = medication.startTimeHour,
            minute = medication.startTimeMinute,
            second = 0,
            nanosecond = 0
        )
        
        val triggerAtMillis = preciseDateTime.toInstant(timeZone).toEpochMilliseconds() - (medication.reminderMinutes * 60 * 1000L)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    override fun cancelNotification(medication: Medication) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medication.id.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
