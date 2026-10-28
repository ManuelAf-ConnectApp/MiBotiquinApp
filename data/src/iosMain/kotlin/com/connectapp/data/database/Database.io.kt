package com.connectapp.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = documentDirectory() + "/MiBotiquinApp.db"
    migrateLegacyDatabase(dbFilePath)
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun migrateLegacyDatabase(destination: String) {
    val manager = NSFileManager.defaultManager
    if (manager.fileExistsAtPath(destination)) return
    // Compatibility-only source name from pre-MiBotiquinApp installs.
    val legacy = documentDirectory() + "/control_pharma.db"
    if (!manager.fileExistsAtPath(legacy)) return
    manager.moveItemAtPath(legacy, destination, error = null)
    listOf("-wal", "-shm", "-journal").forEach { suffix ->
        val source = legacy + suffix
        if (manager.fileExistsAtPath(source)) manager.moveItemAtPath(source, destination + suffix, error = null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    return documentDirectory?.path ?: ""
}
