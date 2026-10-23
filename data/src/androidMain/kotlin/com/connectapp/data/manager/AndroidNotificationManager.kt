package com.connectapp.data.manager

import android.app.NotificationChannel
import android.content.Context
import androidx.core.app.NotificationCompat
import com.connectapp.domain.manager.NotificationManager
import com.connectapp.domain.model.Medication
import android.app.NotificationManager as AndroidNetManager

class AndroidNotificationManager(
    private val context: Context
) : NotificationManager {

    private companion object {
        const val PREFS_NAME = "medication_reminders"
        // AlarmManager no permite listar alarmas: se recuerdan los ids programados para poder
        // cancelar los de medicamentos que ya no deben sonar (borrados, logout, otro usuario...).
        const val KEY_SCHEDULED_IDS = "scheduled_medication_ids"
    }

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

    override fun scheduleReminders(medications: List<Medication>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val previousIds = prefs.getStringSet(KEY_SCHEDULED_IDS, emptySet()).orEmpty()
        val now = System.currentTimeMillis()

        val scheduledIds = medications
            .filter { MedicationReminderAlarms.scheduleNext(context, it, now) }
            .map { it.id.toString() }
            .toSet()

        (previousIds - scheduledIds).forEach { id ->
            id.toLongOrNull()?.let { MedicationReminderAlarms.cancel(context, it) }
        }
        prefs.edit().putStringSet(KEY_SCHEDULED_IDS, scheduledIds).apply()
    }
}
