package com.connectapp.data.backup

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFails
import kotlin.test.assertNotEquals

class EncryptedBackupTest {
    @Test
    fun roundTripRequiresTheBackupPassphrase() = runBlocking {
        val plaintext = "medicación privada".encodeToByteArray()
        val envelope = BackupCipher().encrypt(plaintext, "backup-passphrase".toCharArray())

        assertNotEquals(plaintext.decodeToString(), envelope)
        assertContentEquals(plaintext, BackupCipher().decrypt(envelope, "backup-passphrase".toCharArray()))
        assertFails { BackupCipher().decrypt(envelope, "different-passphrase".toCharArray()) }
        Unit
    }

    @Test
    fun encryptedBackupsUseFreshSaltAndCiphertext() = runBlocking {
        val cipher = BackupCipher()
        val first = cipher.encrypt("same".encodeToByteArray(), "backup-passphrase".toCharArray())
        val second = cipher.encrypt("same".encodeToByteArray(), "backup-passphrase".toCharArray())

        assertNotEquals(first, second)
        Unit
    }

    @Test
    fun tamperedBackupIsRejected() = runBlocking {
        val cipher = BackupCipher()
        val envelope = cipher.encrypt("health data".encodeToByteArray(), "backup-passphrase".toCharArray())
        val tampered = envelope.replaceFirst("ciphertext", "ciphertexu")

        assertFails { cipher.decrypt(tampered, "backup-passphrase".toCharArray()) }
        Unit
    }
}
