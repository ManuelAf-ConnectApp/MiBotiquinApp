package com.connectapp.mibotiquinapp

import androidx.room.RoomDatabase
import com.connectapp.data.database.AppDatabase
import com.connectapp.data.database.getDatabaseBuilder
import com.connectapp.data.manager.IOSNotificationManager
import com.connectapp.data.util.createPreferenceStorage
import com.connectapp.domain.manager.NotificationManager
import com.connectapp.domain.manager.PreferenceStorage
import org.koin.dsl.module

actual val platformModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        getDatabaseBuilder()
    }

    // createdAtStart: la petición de permiso de notificaciones es asíncrona; si el manager
    // se crea de forma perezosa (al guardar el primer medicamento), scheduleNotification()
    // se ejecuta justo después de que aparezca el diálogo del sistema, sin que el usuario haya
    // podido responderlo todavía. Creándolo al arrancar la app, el permiso ya está resuelto
    // mucho antes de que el usuario llegue a guardar un medicamento.
    single<NotificationManager>(createdAtStart = true) { IOSNotificationManager() }

    single<PreferenceStorage> { createPreferenceStorage() }
}