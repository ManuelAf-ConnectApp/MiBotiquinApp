package com.connectapp.mibotiquinapp

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.connectapp.domain.usecase.SyncRemindersUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// AlarmManager olvida todas las alarmas al reiniciar el dispositivo, y las ya programadas se
// calcularon con la hora/zona anterior: en esos casos se vuelven a sincronizar desde la BD.
class RemindersRescheduleReceiver : BroadcastReceiver(), KoinComponent {

    private val syncReminders: SyncRemindersUseCase by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED_ACTIONS) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                syncReminders()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
        )
    }
}
