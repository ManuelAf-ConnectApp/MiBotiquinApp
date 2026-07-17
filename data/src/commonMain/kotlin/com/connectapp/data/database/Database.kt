package com.connectapp.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.connectapp.data.database.dao.MedicationDao
import com.connectapp.data.database.dao.UserDao
import com.connectapp.data.database.entity.UserEntity
import com.connectapp.data.database.entity.MedicationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [UserEntity::class, MedicationEntity::class], version = 2) // Incrementado a 2 por cambio en MedicationEntity
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun medicationDao(): MedicationDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

// 2. Única función de inicialización común (fusión de tus dos funciones)
fun createRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver()) // Obligatorio en KMP para plataformas no-Android
        .setQueryCoroutineContext(Dispatchers.IO) // Asigna los hilos de lectura/escritura
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING) // Optimización de rendimiento
        .fallbackToDestructiveMigration(true) // Permite borrar y recrear la BD si el esquema cambia sin migración manual
        .build() // Se llama una sola vez al final
}