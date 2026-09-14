package com.connectapp.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.connectapp.data.database.dao.MedicationDao
import com.connectapp.data.database.dao.UserDao
import com.connectapp.data.database.entity.UserEntity
import com.connectapp.data.database.entity.MedicationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [UserEntity::class, MedicationEntity::class], version = 4) // v4: FK medications->users con onDelete=CASCADE (evita el error 787 al hacer login)
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
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(connection: SQLiteConnection) {
                super.onCreate(connection)
                connection.prepare(
                    "INSERT INTO users (id, firstName, lastName, email, password, phone, createdAt, updatedAt) " +
                            "VALUES ('admin', 'Admin', 'Control', 'admin@admin.com', 'Admin@2026', '000000000', '2026-08-17', '2026-08-17')"
                ).use { it.step() }
            }
        })
        .setDriver(BundledSQLiteDriver()) // Obligatorio en KMP para plataformas no-Android
        .setQueryCoroutineContext(Dispatchers.IO) // Asigna los hilos de lectura/escritura
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING) // Optimización de rendimiento
        .fallbackToDestructiveMigration(true) // Permite borrar y recrear la BD si el esquema cambia sin migración manual
        .build() // Se llama una sola vez al final
}