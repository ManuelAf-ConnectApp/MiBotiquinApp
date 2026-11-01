package com.connectapp.data.database

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlin.test.*

class RecoveryMigrationTest {
    @Test fun versionSevenAddsNullableRecoveryHashWithoutChangingExistingRows() {
        BundledSQLiteDriver().open(":memory:").use { db ->
            db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, firstName TEXT NOT NULL, lastName TEXT NOT NULL, email TEXT NOT NULL, password TEXT NOT NULL, createdAt TEXT NOT NULL, updatedAt TEXT NOT NULL)")
            db.execSQL("CREATE UNIQUE INDEX index_users_email ON users(email)")
            db.execSQL("CREATE TABLE medications (id INTEGER PRIMARY KEY, codUser TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE, name TEXT NOT NULL)")
            db.execSQL("INSERT INTO users VALUES (17, 'Test', 'User', 'test@example.com', 'existing-hash', 'created', 'updated')")
            db.execSQL("INSERT INTO medications VALUES (9, '17', 'Medication')")
            MIGRATION_7_8.migrate(db)
            db.prepare("SELECT id, password, recoveryHash FROM users").use { row ->
                assertTrue(row.step())
                assertEquals(17L, row.getLong(0))
                assertEquals("existing-hash", row.getText(1))
                assertTrue(row.isNull(2))
            }
            db.prepare("SELECT codUser, name FROM medications").use { row ->
                assertTrue(row.step())
                assertEquals("17", row.getText(0))
                assertEquals("Medication", row.getText(1))
            }
        }
    }
}
