package com.connectapp.data.security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHasherTest {

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun pbkdf2MatchesReferenceVectors() {
        // Valores de referencia calculados con hashlib.pbkdf2_hmac('sha256', ...) de Python.
        assertEquals(
            "120fb6cffcf8b32c43e7225256c4f837a86548c92ccc35480805987cb70be17b",
            pbkdf2HmacSha256("password", "salt".encodeToByteArray(), 1, 32).toHexString()
        )
        assertEquals(
            "8f698c1b2fb577b129cf77a4628899f2c3127a6f634cd51e3fe4daaa9b08b1f8",
            pbkdf2HmacSha256("contraseña", "saltsalt".encodeToByteArray(), 4096, 32).toHexString()
        )
    }

    @Test
    fun hashIsVerifiableAndNeverStoresThePlainPassword() {
        val stored = PasswordHasher.hash("Secreta123")

        assertFalse(stored.contains("Secreta123"))
        assertTrue(PasswordHasher.verify("Secreta123", stored))
        assertFalse(PasswordHasher.verify("secreta123", stored))
        assertFalse(PasswordHasher.needsRehash(stored))
    }

    @Test
    fun samePasswordProducesDifferentHashes() {
        assertNotEquals(PasswordHasher.hash("Secreta123"), PasswordHasher.hash("Secreta123"))
    }

    @Test
    fun legacyPlainTextPasswordsAreAcceptedOnceAndFlaggedForRehash() {
        assertTrue(PasswordHasher.verify("Admin@2026", "Admin@2026"))
        assertFalse(PasswordHasher.verify("otra", "Admin@2026"))
        assertTrue(PasswordHasher.needsRehash("Admin@2026"))
    }

    @Test
    fun malformedHashesNeverVerify() {
        listOf("pbkdf2_sha256\$", "pbkdf2_sha256\$0\$\$", "pbkdf2_sha256\$abc\$AAAA\$AAAA", "pbkdf2_sha256\$1\$%%%\$AAAA")
            .forEach { assertFalse(PasswordHasher.verify("x", it), it) }
    }
}
