package com.connectapp.mibotiquinapp

import com.connectapp.data.catalogue.AndroidCatalogueHttpClient
import com.connectapp.data.datasource.CatalogueHttpClient
import com.connectapp.domain.model.AppInfo
import androidx.room.RoomDatabase
import com.connectapp.data.database.AppDatabase
import com.connectapp.data.database.getDatabaseBuilder
import com.connectapp.data.manager.AndroidNotificationManager
import com.connectapp.data.security.AndroidSecureStorage
import com.connectapp.data.security.SecureStorage
import com.connectapp.data.util.createPreferenceStorage
import com.connectapp.domain.manager.NotificationManager
import com.connectapp.domain.manager.PreferenceStorage
import org.koin.dsl.module


actual val platformModule = module {
    single {
        val context = get<android.content.Context>()
        AppInfo(context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—")
    }
    single<CatalogueHttpClient> { AndroidCatalogueHttpClient() }
    single<RoomDatabase.Builder<AppDatabase>> {
        getDatabaseBuilder(context = get())
    }

    single<NotificationManager> { AndroidNotificationManager(
        context = get()
    ) }

    single<PreferenceStorage> { createPreferenceStorage(context = get<android.content.Context>()) }

    single<SecureStorage> { AndroidSecureStorage(context = get()) }
}