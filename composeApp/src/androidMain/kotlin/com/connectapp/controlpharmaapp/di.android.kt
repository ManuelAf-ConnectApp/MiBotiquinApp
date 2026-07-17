package com.connectapp.controlpharmaapp

import androidx.room.RoomDatabase
import com.connectapp.data.database.AppDatabase
import com.connectapp.data.database.createRoomDatabase
import com.connectapp.data.database.getDatabaseBuilder
import com.connectapp.data.manager.AndroidNotificationManager
import com.connectapp.domain.manager.NotificationManager
import org.koin.dsl.module


actual val platformModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        getDatabaseBuilder(context = get())
    }

    single<NotificationManager> { AndroidNotificationManager(
        context = get()
    ) }
}