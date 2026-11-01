// Generated from checked-in Room schemas by scripts/generate_migration_fixtures.py.
// Do not edit SQL or identity hashes here; regenerate from the original exports.
package com.connectapp.data.database

internal fun historicalSchema(version: Int): List<String> = when (version) {
    1 -> listOf(
        """CREATE TABLE IF NOT EXISTS `users` (`id` TEXT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `email` TEXT NOT NULL, `phone` TEXT NOT NULL, `createdAt` TEXT NOT NULL, `updatedAt` TEXT NOT NULL, PRIMARY KEY(`id`))""",
        """CREATE TABLE IF NOT EXISTS `medications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `codUser` TEXT NOT NULL, `dosesPerDay` INTEGER NOT NULL, `quantity` REAL NOT NULL, `quantityUnit` TEXT NOT NULL, `durationDays` INTEGER NOT NULL, `startDate` INTEGER, `endDate` INTEGER, `reminderMinutes` INTEGER NOT NULL, `startTimeHour` INTEGER NOT NULL, `startTimeMinute` INTEGER NOT NULL, FOREIGN KEY(`codUser`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )""",
        """CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)""",
        """INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '117c9924c3d212c6db50f741631500eb')""",
        """PRAGMA user_version = 1""",
    )
    2 -> listOf(
        """CREATE TABLE IF NOT EXISTS `users` (`id` TEXT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `email` TEXT NOT NULL, `phone` TEXT NOT NULL, `createdAt` TEXT NOT NULL, `updatedAt` TEXT NOT NULL, PRIMARY KEY(`id`))""",
        """CREATE TABLE IF NOT EXISTS `medications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `codUser` TEXT NOT NULL, `dosesPerDay` INTEGER NOT NULL, `quantity` REAL NOT NULL, `quantityUnit` TEXT NOT NULL, `durationDays` INTEGER NOT NULL, `startDate` INTEGER, `endDate` INTEGER, `reminderMinutes` INTEGER NOT NULL, `startTimeHour` INTEGER NOT NULL, `startTimeMinute` INTEGER NOT NULL, FOREIGN KEY(`codUser`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )""",
        """CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)""",
        """INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '117c9924c3d212c6db50f741631500eb')""",
        """PRAGMA user_version = 2""",
    )
    3 -> listOf(
        """CREATE TABLE IF NOT EXISTS `users` (`id` TEXT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `email` TEXT NOT NULL, `password` TEXT NOT NULL, `phone` TEXT NOT NULL, `createdAt` TEXT NOT NULL, `updatedAt` TEXT NOT NULL, PRIMARY KEY(`id`))""",
        """CREATE TABLE IF NOT EXISTS `medications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `codUser` TEXT NOT NULL, `dosesPerDay` INTEGER NOT NULL, `quantity` REAL NOT NULL, `quantityUnit` TEXT NOT NULL, `durationDays` INTEGER NOT NULL, `startDate` INTEGER, `endDate` INTEGER, `reminderMinutes` INTEGER NOT NULL, `startTimeHour` INTEGER NOT NULL, `startTimeMinute` INTEGER NOT NULL, FOREIGN KEY(`codUser`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )""",
        """CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)""",
        """INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '2ebbd8024237ab232d37bcea09ec9a7b')""",
        """PRAGMA user_version = 3""",
    )
    4 -> listOf(
        """CREATE TABLE IF NOT EXISTS `users` (`id` TEXT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `email` TEXT NOT NULL, `password` TEXT NOT NULL, `createdAt` TEXT NOT NULL, `updatedAt` TEXT NOT NULL, PRIMARY KEY(`id`))""",
        """CREATE TABLE IF NOT EXISTS `medications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `codUser` TEXT NOT NULL, `dosesPerDay` INTEGER NOT NULL, `quantity` REAL NOT NULL, `quantityUnit` TEXT NOT NULL, `durationDays` INTEGER NOT NULL, `startDate` INTEGER, `endDate` INTEGER, `reminderMinutes` INTEGER NOT NULL, `startTimeHour` INTEGER NOT NULL, `startTimeMinute` INTEGER NOT NULL, FOREIGN KEY(`codUser`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""",
        """CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)""",
        """INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '1c6d27636570b6f20a9c697d1b759d1a')""",
        """PRAGMA user_version = 4""",
    )
    5 -> listOf(
        """CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `email` TEXT NOT NULL, `password` TEXT NOT NULL, `createdAt` TEXT NOT NULL, `updatedAt` TEXT NOT NULL)""",
        """CREATE TABLE IF NOT EXISTS `medications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `codUser` TEXT NOT NULL, `dosesPerDay` INTEGER NOT NULL, `quantity` REAL NOT NULL, `quantityUnit` TEXT NOT NULL, `durationDays` INTEGER NOT NULL, `startDate` INTEGER, `endDate` INTEGER, `reminderMinutes` INTEGER NOT NULL, `startTimeHour` INTEGER NOT NULL, `startTimeMinute` INTEGER NOT NULL, FOREIGN KEY(`codUser`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""",
        """CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)""",
        """INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '8568ed8cd8edec1ffd27532ec38474bb')""",
        """PRAGMA user_version = 5""",
    )
    6 -> listOf(
        """CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `email` TEXT NOT NULL, `password` TEXT NOT NULL, `createdAt` TEXT NOT NULL, `updatedAt` TEXT NOT NULL)""",
        """CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`)""",
        """CREATE TABLE IF NOT EXISTS `medications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `codUser` TEXT NOT NULL, `dosesPerDay` INTEGER NOT NULL, `quantity` REAL NOT NULL, `quantityUnit` TEXT NOT NULL, `durationDays` INTEGER NOT NULL, `startDate` INTEGER, `endDate` INTEGER, `reminderMinutes` INTEGER NOT NULL, `startTimeHour` INTEGER NOT NULL, `startTimeMinute` INTEGER NOT NULL, FOREIGN KEY(`codUser`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""",
        """CREATE INDEX IF NOT EXISTS `index_medications_codUser` ON `medications` (`codUser`)""",
        """CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)""",
        """INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'b546820322fe116b09361ae5399efa05')""",
        """PRAGMA user_version = 6""",
    )
    7 -> listOf(
        """CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `email` TEXT NOT NULL, `password` TEXT NOT NULL, `createdAt` TEXT NOT NULL, `updatedAt` TEXT NOT NULL)""",
        """CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`)""",
        """CREATE TABLE IF NOT EXISTS `medications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `codUser` TEXT NOT NULL, `dosesPerDay` INTEGER NOT NULL, `quantity` REAL NOT NULL, `quantityUnit` TEXT NOT NULL, `durationDays` INTEGER NOT NULL, `startDate` INTEGER, `endDate` INTEGER, `reminderMinutes` INTEGER NOT NULL, `startTimeHour` INTEGER NOT NULL, `startTimeMinute` INTEGER NOT NULL, FOREIGN KEY(`codUser`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""",
        """CREATE INDEX IF NOT EXISTS `index_medications_codUser` ON `medications` (`codUser`)""",
        """CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)""",
        """INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'b546820322fe116b09361ae5399efa05')""",
        """PRAGMA user_version = 7""",
    )
    8 -> listOf(
        """CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `email` TEXT NOT NULL, `password` TEXT NOT NULL, `createdAt` TEXT NOT NULL, `updatedAt` TEXT NOT NULL, `recoveryHash` TEXT)""",
        """CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`)""",
        """CREATE TABLE IF NOT EXISTS `medications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `codUser` TEXT NOT NULL, `dosesPerDay` INTEGER NOT NULL, `quantity` REAL NOT NULL, `quantityUnit` TEXT NOT NULL, `durationDays` INTEGER NOT NULL, `startDate` INTEGER, `endDate` INTEGER, `reminderMinutes` INTEGER NOT NULL, `startTimeHour` INTEGER NOT NULL, `startTimeMinute` INTEGER NOT NULL, FOREIGN KEY(`codUser`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""",
        """CREATE INDEX IF NOT EXISTS `index_medications_codUser` ON `medications` (`codUser`)""",
        """CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)""",
        """INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '4cc81df2ca67087a443fe3c0eba4014f')""",
        """PRAGMA user_version = 8""",
    )
    else -> error("No exported fixture for version $version")
}
