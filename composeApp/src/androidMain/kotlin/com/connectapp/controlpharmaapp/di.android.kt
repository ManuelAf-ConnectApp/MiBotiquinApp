package com.connectapp.controlpharmaapp

import androidx.room.RoomDatabase
import com.connectapp.data.database.AppDatabase
import com.connectapp.data.database.getDatabaseBuilder
import com.connectapp.data.manager.AndroidNotificationManager
import com.connectapp.data.util.createPreferenceStorage
import com.connectapp.domain.manager.NotificationManager
import com.connectapp.domain.manager.PreferenceStorage
import org.koin.dsl.module


actual val platformModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        getDatabaseBuilder(context = get())
    }

    single<NotificationManager> { AndroidNotificationManager(
        context = get()
    ) }

    single<PreferenceStorage> { createPreferenceStorage(context = get<android.content.Context>()) }
}