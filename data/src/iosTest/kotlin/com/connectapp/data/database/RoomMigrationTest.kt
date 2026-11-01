package com.connectapp.data.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import kotlin.test.*

/** Opens real historical files with the production builder and generated Room validator. */
@OptIn(ExperimentalForeignApi::class)
class RoomMigrationTest {
    @Test fun versionOneUpgradesWithoutLosingMedication() = runBlocking { verifyUpgrade(1) }
    @Test fun versionTwoUpgradesWithoutLosingMedication() = runBlocking { verifyUpgrade(2) }
    @Test fun versionThreeUpgradesWithoutLosingMedication() = runBlocking { verifyUpgrade(3) }
    @Test fun versionFourUpgradesWithoutLosingMedication() = runBlocking { verifyUpgrade(4) }
    @Test fun versionFiveUpgradesWithoutLosingMedication() = runBlocking { verifyUpgrade(5) }
    @Test fun versionSixUpgradesWithoutLosingMedication() = runBlocking { verifyUpgrade(6) }
    @Test fun versionSevenUpgradesWithoutLosingMedication() = runBlocking { verifyUpgrade(7) }
    @Test fun versionEightUpgradesWithoutLosingRecoveryOrMedication() = runBlocking { verifyUpgrade(8) }

    private suspend fun verifyUpgrade(version: Int) = withHistoricalFile(version) { path ->
        withRoom(path) { db ->
            // First DAO access runs the real migrations and Room's complete schema validation.
            val user = assertNotNull(db.userDao().getUserByEmail("test@example.com"))
            assertEquals("test@example.com", user.email)
            assertEquals(if (version < 3) "" else "unchanged-password", user.password)
            assertEquals(if (version >= 8) "unchanged-recovery-hash" else null, user.recoveryHash)
            val medication = assertNotNull(db.medicationDao().getMedicationById(9))
            assertEquals(user.id.toString(), medication.codUser)
            assertEquals(0.3, medication.quantity)
            assertEquals("legacy-unit", medication.quantityUnit)
            assertNull(medication.quantityMinor)
            assertFalse(medication.isPaused)
            assertFalse(medication.isIndefinite)
            assertNull(medication.catalogueCode)
            assertNull(db.stockDao().account(9, "17"))
            // Check that new columns work on the migrated database, rather than only reading it.
            db.medicationDao().insertMedication(medication.copy(
                quantity = 0.25, quantityMinor = 250, quantityUnit = "tablet",
                notes = "migrated note", isPaused = true
            ))
        }
        // Reopen to exercise the updated identity hash and persistent writes.
        withRoom(path) { db ->
            val medication = assertNotNull(db.medicationDao().getMedicationById(9))
            assertEquals(250L, medication.quantityMinor)
            assertEquals("migrated note", medication.notes)
            assertTrue(medication.isPaused)
            assertEquals(1, db.medicationDao().getUserMedicationsByCodUserOnce(medication.codUser).size)
        }
        BundledSQLiteDriver().open(path).use { connection ->
            connection.prepare("PRAGMA user_version").use { row ->
                assertTrue(row.step())
                assertEquals(14L, row.getLong(0))
            }
            connection.prepare("PRAGMA foreign_key_check").use { row -> assertFalse(row.step()) }
        }
    }

    @Test fun unsupportedVersionFailsWithoutErasingTheOldDatabase() = runBlocking {
        withHistoricalFile(1) { path ->
            BundledSQLiteDriver().open(path).use { it.execSQL("PRAGMA user_version = 0") }
            withRoom(path) { db ->
                assertFailsWith<IllegalStateException> { db.userDao().getUserByEmail("test@example.com") }
            }
            BundledSQLiteDriver().open(path).use { connection ->
                connection.prepare("SELECT email FROM users WHERE id = 'legacy-user'").use { row ->
                    assertTrue(row.step())
                    assertEquals("test@example.com", row.getText(0))
                }
                connection.prepare("SELECT name, quantity FROM medications WHERE id = 9").use { row ->
                    assertTrue(row.step())
                    assertEquals("Test medication", row.getText(0))
                    assertEquals(0.3, row.getDouble(1))
                }
                connection.prepare("PRAGMA user_version").use { row ->
                    assertTrue(row.step())
                    assertEquals(0L, row.getLong(0))
                }
            }
        }
    }

    private suspend fun withRoom(path: String, block: suspend (AppDatabase) -> Unit) {
        val database = createRoomDatabase(Room.databaseBuilder<AppDatabase>(name = path))
        try {
            block(database)
        } finally {
            database.close()
        }
    }

    private suspend fun withHistoricalFile(version: Int, block: suspend (String) -> Unit) {
        val path = NSTemporaryDirectory() + "mibotiquin-migration-test-${NSUUID.UUID().UUIDString()}.db"
        try {
            BundledSQLiteDriver().open(path).use { connection ->
                historicalSchema(version).forEach { connection.execSQL(it) }
                val userColumns = when (version) {
                    1, 2 -> "id, firstName, lastName, email, phone, createdAt, updatedAt"
                    3 -> "id, firstName, lastName, email, password, phone, createdAt, updatedAt"
                    else -> "id, firstName, lastName, email, password, createdAt, updatedAt"
                }
                val userId = if (version <= 4) "'legacy-user'" else "17"
                val userValues = when (version) {
                    1, 2 -> "$userId, 'Test', 'User', 'test@example.com', '000000000', 'created', 'updated'"
                    3 -> "$userId, 'Test', 'User', 'test@example.com', 'unchanged-password', '000000000', 'created', 'updated'"
                    else -> "$userId, 'Test', 'User', 'test@example.com', 'unchanged-password', 'created', 'updated'"
                }
                connection.execSQL("INSERT INTO users ($userColumns) VALUES ($userValues)")
                connection.execSQL("INSERT INTO medications (id, name, codUser, dosesPerDay, quantity, quantityUnit, durationDays, startDate, endDate, reminderMinutes, startTimeHour, startTimeMinute) VALUES (9, 'Test medication', ${if (version <= 4) "'legacy-user'" else "'17'"}, 1, 0.3, 'legacy-unit', 1, 0, 0, 0, 8, 0)")
                if (version >= 8) connection.execSQL("UPDATE users SET recoveryHash = 'unchanged-recovery-hash' WHERE id = 17")
            }
            block(path)
        } finally {
            for (suffix in listOf("", "-wal", "-shm", "-journal")) {
                NSFileManager.defaultManager.removeItemAtPath(path + suffix, error = null)
            }
        }
    }
}
