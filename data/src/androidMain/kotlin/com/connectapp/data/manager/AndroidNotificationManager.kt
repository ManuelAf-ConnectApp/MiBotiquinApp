package com.connectapp.data.manager

import android.app.NotificationChannel
import android.content.Context
import androidx.core.app.NotificationCompat
import com.connectapp.domain.manager.NotificationManager
import com.connectapp.domain.model.DoseReminder
import android.app.NotificationManager as AndroidNetManager
import android.app.AlarmManager
import android.os.Build
import android.media.AudioAttributes
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.connectapp.domain.model.NotificationDiagnostics

class AndroidNotificationManager(
    private val context: Context
) : NotificationManager {

    private companion object {
        const val PREFS_NAME = "medication_reminders"
        const val CHANNEL_ID = "medication_reminders_v2"
        // AlarmManager no permite listar alarmas: se recuerdan los ids programados para poder
        // cancelar los de medicamentos que ya no deben sonar (borrados, logout, otro usuario...).
        const val KEY_SCHEDULED_IDS = "scheduled_medication_ids"
    }

    init {
        createNotificationChannel()
    }

    override fun showNotification(title: String, body: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
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
            CHANNEL_ID,
            channelName,
            AndroidNetManager.IMPORTANCE_HIGH
        )
        channel.setSound(
            Settings.System.DEFAULT_NOTIFICATION_URI,
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
        )
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNetManager
        manager.createNotificationChannel(channel)
    }

    override fun scheduleReminders(reminders: List<DoseReminder>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val legacyIds = prefs.getStringSet(KEY_SCHEDULED_IDS, emptySet()).orEmpty()
        legacyIds.forEach { it.toLongOrNull()?.let { id -> MedicationReminderAlarms.cancelLegacy(context, id) } }
        val previous = prefs.getStringSet("scheduled_dose_ids", emptySet()).orEmpty()
        val upcoming = reminders.filter { it.reminderAt > System.currentTimeMillis() }
        val ids = upcoming.map { it.eventId }.toSet()
        (previous - ids).forEach { MedicationReminderAlarms.cancel(context, it) }
        upcoming.forEach { MedicationReminderAlarms.schedule(context, it) }
        prefs.edit().remove(KEY_SCHEDULED_IDS).putStringSet("scheduled_dose_ids", ids).apply()
    }

    override fun diagnostics(): NotificationDiagnostics {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val exact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms() else true
        return NotificationDiagnostics(
            systemEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled(),
            exactAlarmsAvailable = exact,
            scheduledCount = prefs.getStringSet("scheduled_dose_ids", emptySet()).orEmpty().size,
            detail = "Android"
        )
    }
}
