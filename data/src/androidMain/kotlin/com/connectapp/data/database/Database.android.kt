package com.connectapp.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("MiBotiquinApp.db")
    migrateLegacyDatabase(appContext, dbFile)
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

private fun migrateLegacyDatabase(context: Context, destination: java.io.File) {
    if (destination.exists()) return
    // Compatibility-only source name from pre-MiBotiquinApp installs. Move WAL companions too.
    val legacy = context.getDatabasePath("control_pharma.db")
    if (!legacy.exists()) return
    legacy.renameTo(destination)
    listOf("-wal", "-shm", "-journal").forEach { suffix ->
        val source = java.io.File(legacy.path + suffix)
        if (source.exists()) source.renameTo(java.io.File(destination.path + suffix))
    }
}
