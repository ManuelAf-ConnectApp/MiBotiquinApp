package com.connectapp.data

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.media.AudioAttributes
import android.provider.Settings
import android.app.Notification
import androidx.core.app.NotificationCompat
import com.connectapp.data.manager.MedicationReminderAlarms
import com.connectapp.domain.manager.PreferenceKeys
import com.connectapp.domain.manager.PreferenceStorage
import com.connectapp.domain.model.DoseStatus
import com.connectapp.domain.repository.DoseRepository
import com.connectapp.domain.usecase.SyncRemindersUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MedicationAlarmReceiver : BroadcastReceiver(), KoinComponent {
    private val doses: DoseRepository by inject()
    private val syncReminders: SyncRemindersUseCase by inject()
    private val preferences: PreferenceStorage by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getStringExtra(MedicationReminderAlarms.EXTRA_EVENT_ID) ?: return
        val result = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val dose = doses.getDose(eventId).getOrNull()
                if (preferences.getBoolean(PreferenceKeys.NOTIFICATIONS_ENABLED, true) && dose?.status == DoseStatus.PENDING &&
                    dose.reminderAt <= System.currentTimeMillis()) {
                    showNotification(context, eventId)
                }
                // Refill the bounded planning window after delivery; no old medication JSON is trusted.
                syncReminders()
            } finally { result.finish() }
        }
    }

    private fun showNotification(context: Context, eventId: String) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val spanish = context.resources.configuration.locales[0].language.startsWith("es")
        // Android does not allow changing a channel's sound/importance after it has been created.
        // A new id ensures installations that already had the reminder channel receive alarm
        // behaviour as well.
        val channelId = "medication_alarms_v3"
        manager.createNotificationChannel(NotificationChannel(channelId,
            if (spanish) "Alarmas de medicación" else "Medication alarms", NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(Settings.System.DEFAULT_NOTIFICATION_URI,
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        })
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
        launch.apply {
            action = MedicationReminderAlarms.ACTION_OPEN_DOSE
            data = Uri.Builder().scheme("mibotiquin").authority("dose").appendPath(eventId).build()
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MedicationReminderAlarms.EXTRA_EVENT_ID, eventId)
            putExtra(MedicationReminderAlarms.EXTRA_SHOW_AS_ALARM, true)
        }
        val contentIntent = PendingIntent.getActivity(context, eventId.hashCode(), launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(if (spanish) "Es hora de tu medicación" else "Time for your medication")
            .setContentText(if (spanish) "Toca para revisar tu toma pendiente." else "Tap to review your pending dose.")
            .setContentIntent(contentIntent)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(contentIntent, true)
            .setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_MAX)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .build()
        manager.notify(eventId, 0, notification)
    }
}
