package com.connectapp.data.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.connectapp.data.MedicationAlarmReceiver
import com.connectapp.domain.model.DoseReminder

internal object MedicationReminderAlarms {
    const val EXTRA_EVENT_ID = "dose_event_id"
    const val ACTION_OPEN_DOSE = "com.connectapp.OPEN_DOSE"

    private fun intent(context: Context, eventId: String) = Intent(context, MedicationAlarmReceiver::class.java).apply {
        data = Uri.Builder().scheme("mibotiquin").authority("reminder").appendPath(eventId).build()
        putExtra(EXTRA_EVENT_ID, eventId)
    }

    fun schedule(context: Context, reminder: DoseReminder) {
        val pending = PendingIntent.getBroadcast(context, 0, intent(context, reminder.eventId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarms.canScheduleExactAlarms()) {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.reminderAt, pending)
        } else alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.reminderAt, pending)
    }

    fun cancel(context: Context, eventId: String) {
        val pending = PendingIntent.getBroadcast(context, 0, intent(context, eventId),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE) ?: return
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending)
        pending.cancel()
    }

    fun cancelLegacy(context: Context, medicationId: Long) {
        val pending = PendingIntent.getBroadcast(context, medicationId.toInt(), Intent(context, MedicationAlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE) ?: return
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending)
        pending.cancel()
    }
}
