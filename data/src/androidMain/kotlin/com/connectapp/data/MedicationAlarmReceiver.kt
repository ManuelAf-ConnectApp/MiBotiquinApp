package com.connectapp.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.connectapp.data.manager.MedicationReminderAlarms
import com.connectapp.domain.model.Medication
import kotlinx.serialization.json.Json

class MedicationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medication = intent.getStringExtra(MedicationReminderAlarms.EXTRA_MEDICATION)
            ?.let { runCatching { Json.decodeFromString<Medication>(it) }.getOrNull() }
            ?: return
        val triggerAtMillis = intent.getLongExtra(MedicationReminderAlarms.EXTRA_TRIGGER_AT, 0L)

        // 1. Mostrar la notificación nativa inmediatamente
        showNotification(context, medication.id.toInt(), medication.name)

        // 2. Encadenar el siguiente aviso. Se calcula a partir del instante en que debía sonar
        // éste (o de ahora, si llegó tarde) para no repetir el mismo aviso.
        MedicationReminderAlarms.scheduleNext(
            context = context,
            medication = medication,
            afterMillis = maxOf(triggerAtMillis, System.currentTimeMillis())
        )
    }

    fun showNotification(context: Context, id: Int, title: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "medication_reminders"

        val locale = context.resources.configuration.locales[0]
        val isSpanish = locale.language.lowercase().startsWith("es")

        val channelName = if (isSpanish) "Recordatorios de Medicación" else "Medication Reminders"
        val notifTitle = if (isSpanish) "Hora de tu medicación" else "Time for your medication"
        val notifBody = if (isSpanish) "No olvides tomar tu dosis de: $title" else "Don't forget to take your dose of: $title"

        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(notifTitle)
            .setContentText(notifBody)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(id, notification)
    }
}
