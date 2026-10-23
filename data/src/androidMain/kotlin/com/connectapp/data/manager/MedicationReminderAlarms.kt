package com.connectapp.data.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.connectapp.data.MedicationAlarmReceiver
import com.connectapp.domain.model.Medication
import com.connectapp.domain.reminder.DoseSchedule
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json

// Android sólo mantiene una alarma por medicamento: la del próximo aviso. Al sonar,
// MedicationAlarmReceiver programa la siguiente calculándola de nuevo con DoseSchedule
// (nunca "ahora + intervalo"), de modo que el horario no deriva aunque la alarma llegue tarde.
internal object MedicationReminderAlarms {

    const val EXTRA_MEDICATION = "MEDICATION_JSON"
    const val EXTRA_TRIGGER_AT = "TRIGGER_AT"

    /** Programa el próximo aviso posterior a [afterMillis]; false si el tratamiento ya terminó. */
    fun scheduleNext(context: Context, medication: Medication, afterMillis: Long): Boolean {
        val triggerAtMillis = DoseSchedule.upcomingReminders(
            medication = medication,
            afterMillis = afterMillis,
            timeZone = TimeZone.currentSystemDefault(),
            limit = 1
        ).firstOrNull()

        if (triggerAtMillis == null) {
            cancel(context, medication.id)
            return false
        }

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra(EXTRA_MEDICATION, Json.encodeToString(medication))
            putExtra(EXTRA_TRIGGER_AT, triggerAtMillis)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medication.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
        return true
    }

    fun cancel(context: Context, medicationId: Long) {
        // Los extras no cuentan para Intent.filterEquals: basta con el mismo componente y requestCode.
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId.toInt(),
            Intent(context, MedicationAlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
