package com.connectapp.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.connectapp.data.database.dao.MedicationDao
import com.connectapp.data.database.dao.UserDao
import com.connectapp.data.database.entity.UserEntity
import com.connectapp.data.database.entity.MedicationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [UserEntity::class, MedicationEntity::class], version = 7) // v7: se elimina la cuenta admin sembrada con contraseña conocida
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun medicationDao(): MedicationDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

// Migración explícita para no perder los medicamentos existentes al actualizar: hasta v5 la
// tabla users sólo contenía una fila (se vaciaba en cada login/logout), así que el índice
// único sobre email no puede fallar por duplicados.
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_medications_codUser` ON `medications` (`codUser`)")
    }
}

// Hasta v6 toda instalación nueva creaba admin@admin.com / Admin@2026, una contraseña pública
// que daba acceso a esa cuenta en cualquier dispositivo. Se borra salvo que alguien la esté
// usando de verdad (tenga medicamentos), para no eliminar datos de un usuario.
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "DELETE FROM `users` WHERE `email` = 'admin@admin.com' AND `password` = 'Admin@2026' " +
                "AND CAST(`id` AS TEXT) NOT IN (SELECT `codUser` FROM `medications`)"
        )
    }
}

// 2. Única función de inicialización común (fusión de tus dos funciones)
fun createRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
        .setDriver(BundledSQLiteDriver()) // Obligatorio en KMP para plataformas no-Android
        .setQueryCoroutineContext(Dispatchers.IO) // Asigna los hilos de lectura/escritura
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING) // Optimización de rendimiento
        // Sólo se recrea la BD desde esquemas anteriores a v5 (sin migraciones escritas). Para
        // cualquier cambio futuro hay que añadir una Migration: si falta, la app falla en lugar
        // de borrar en silencio los medicamentos del usuario.
        .fallbackToDestructiveMigrationFrom(true, 1, 2, 3, 4)
        .build() // Se llama una sola vez al final
}