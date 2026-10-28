package com.connectapp.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.connectapp.data.database.dao.StockDao
import com.connectapp.data.database.entity.StockAccountEntity
import com.connectapp.data.database.entity.StockMovementEntity
import com.connectapp.data.database.dao.DoseEventDao
import com.connectapp.data.database.entity.DoseEventEntity
import com.connectapp.data.database.dao.MedicationDao
import com.connectapp.data.database.dao.UserDao
import com.connectapp.data.database.entity.UserEntity
import com.connectapp.data.database.entity.MedicationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [UserEntity::class, MedicationEntity::class, DoseEventEntity::class, StockAccountEntity::class, StockMovementEntity::class], version = 15) // v15: horarios por día de semana
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
    abstract fun doseEventDao(): DoseEventDao
    abstract fun userDao(): UserDao
    abstract fun medicationDao(): MedicationDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

// Las versiones 1--4 guardaban el identificador del usuario como texto, mientras que v5 lo
// convirtió en una clave entera autogenerada. Se reconstruyen ambas tablas para conservar la
// relación medicamento--usuario, incluso si el identificador histórico no era numérico.
private fun legacyUsersToV5Migration(fromVersion: Int) = object : Migration(fromVersion, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE `legacy_user_ids` (`oldId` TEXT NOT NULL, `newId` INTEGER NOT NULL, PRIMARY KEY(`oldId`))")
        connection.execSQL("CREATE TABLE `users_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `email` TEXT NOT NULL, `password` TEXT NOT NULL, `createdAt` TEXT NOT NULL, `updatedAt` TEXT NOT NULL)")
        connection.execSQL("INSERT INTO `legacy_user_ids` (`oldId`, `newId`) SELECT CAST(`id` AS TEXT), CASE WHEN CAST(`id` AS TEXT) <> '' AND CAST(`id` AS TEXT) NOT GLOB '*[^0-9]*' AND CAST(`id` AS INTEGER) > 0 THEN CAST(`id` AS INTEGER) ELSE rowid + 1000000000 END FROM `users`")
        val passwordExpression = if (fromVersion < 3) "''" else "`password`"
        connection.execSQL("INSERT INTO `users_new` (`id`, `firstName`, `lastName`, `email`, `password`, `createdAt`, `updatedAt`) SELECT m.`newId`, u.`firstName`, u.`lastName`, u.`email`, $passwordExpression, u.`createdAt`, u.`updatedAt` FROM `users` u JOIN `legacy_user_ids` m ON m.`oldId` = CAST(u.`id` AS TEXT)")
        connection.execSQL("CREATE TABLE `medications_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `codUser` TEXT NOT NULL, `dosesPerDay` INTEGER NOT NULL, `quantity` REAL NOT NULL, `quantityUnit` TEXT NOT NULL, `durationDays` INTEGER NOT NULL, `startDate` INTEGER, `endDate` INTEGER, `reminderMinutes` INTEGER NOT NULL, `startTimeHour` INTEGER NOT NULL, `startTimeMinute` INTEGER NOT NULL, FOREIGN KEY(`codUser`) REFERENCES `users_new`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
        connection.execSQL("INSERT INTO `medications_new` (`id`, `name`, `codUser`, `dosesPerDay`, `quantity`, `quantityUnit`, `durationDays`, `startDate`, `endDate`, `reminderMinutes`, `startTimeHour`, `startTimeMinute`) SELECT medication.`id`, medication.`name`, CAST(m.`newId` AS TEXT), medication.`dosesPerDay`, medication.`quantity`, medication.`quantityUnit`, medication.`durationDays`, medication.`startDate`, medication.`endDate`, medication.`reminderMinutes`, medication.`startTimeHour`, medication.`startTimeMinute` FROM `medications` medication JOIN `legacy_user_ids` m ON m.`oldId` = medication.`codUser`")
        connection.execSQL("DROP TABLE `medications`")
        connection.execSQL("DROP TABLE `users`")
        connection.execSQL("ALTER TABLE `users_new` RENAME TO `users`")
        connection.execSQL("ALTER TABLE `medications_new` RENAME TO `medications`")
        connection.execSQL("DROP TABLE `legacy_user_ids`")
    }
}

val MIGRATION_1_5 = legacyUsersToV5Migration(1)
val MIGRATION_2_5 = legacyUsersToV5Migration(2)
val MIGRATION_3_5 = legacyUsersToV5Migration(3)
val MIGRATION_4_5 = legacyUsersToV5Migration(4)

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

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `users` ADD COLUMN `recoveryHash` TEXT")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(connection: SQLiteConnection) {
        // Preserve legacy quantities verbatim; exact amounts are populated on explicit save.
        connection.execSQL("ALTER TABLE `medications` ADD COLUMN `quantityMinor` INTEGER")
        connection.execSQL("ALTER TABLE `medications` ADD COLUMN `isIndefinite` INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE `medications` ADD COLUMN `isPaused` INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE `medications` ADD COLUMN `notes` TEXT NOT NULL DEFAULT ''")
        connection.execSQL("ALTER TABLE `medications` ADD COLUMN `pharmacologicalDose` TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `dose_events` (`id` TEXT NOT NULL, `medicationId` INTEGER NOT NULL, `codUser` TEXT NOT NULL, `scheduledAt` INTEGER NOT NULL, `reminderAt` INTEGER NOT NULL, `status` TEXT NOT NULL, `resolvedAt` INTEGER, `quantityMinor` INTEGER NOT NULL, `quantityUnit` TEXT NOT NULL, `notes` TEXT NOT NULL, `pharmacologicalDose` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`medicationId`) REFERENCES `medications`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_dose_events_medicationId_scheduledAt` ON `dose_events` (`medicationId`, `scheduledAt`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_dose_events_codUser_status_scheduledAt` ON `dose_events` (`codUser`, `status`, `scheduledAt`)")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `medications` ADD COLUMN `catalogueCode` TEXT")
        connection.execSQL("ALTER TABLE `medications` ADD COLUMN `catalogueName` TEXT")
        connection.execSQL("ALTER TABLE `medications` ADD COLUMN `catalogueRetrievedAt` INTEGER")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `stock_accounts` (`medicationId` INTEGER NOT NULL, `codUser` TEXT NOT NULL, `quantityUnit` TEXT NOT NULL, `initialMinor` INTEGER NOT NULL, `balanceMinor` INTEGER NOT NULL, `lowThresholdMinor` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`medicationId`), FOREIGN KEY(`medicationId`) REFERENCES `medications`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_accounts_codUser` ON `stock_accounts` (`codUser`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `stock_movements` (`id` TEXT NOT NULL, `medicationId` INTEGER NOT NULL, `codUser` TEXT NOT NULL, `kind` TEXT NOT NULL, `deltaMinor` INTEGER NOT NULL, `requestedMinor` INTEGER NOT NULL, `quantityUnit` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `reason` TEXT NOT NULL, `eventId` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`medicationId`) REFERENCES `stock_accounts`(`medicationId`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`eventId`) REFERENCES `dose_events`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_movements_medicationId_createdAt` ON `stock_movements` (`medicationId`, `createdAt`)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_stock_movements_eventId` ON `stock_movements` (`eventId`)")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `medications` ADD COLUMN `customSchedule` TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `dose_events` ADD COLUMN `snoozedUntil` INTEGER DEFAULT NULL")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `medications` ADD COLUMN `isArchived` INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE `medications` ADD COLUMN `weekdaySchedule` TEXT NOT NULL DEFAULT ''")
    }
}

// Única función de inicialización común.
fun createRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        .addMigrations(MIGRATION_1_5, MIGRATION_2_5, MIGRATION_3_5, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15)
        .setDriver(BundledSQLiteDriver()) // Obligatorio en KMP para plataformas no-Android
        .setQueryCoroutineContext(Dispatchers.IO) // Asigna los hilos de lectura/escritura
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING) // Optimización de rendimiento
        // No destructive fallback: una versión desconocida conserva sus datos y falla al abrir.
        .build() // Se llama una sola vez al final
}
