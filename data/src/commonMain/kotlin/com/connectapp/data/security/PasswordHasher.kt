package com.connectapp.data.security

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** PBKDF2-HMAC-SHA256 con la implementación nativa de cada plataforma. */
internal expect fun pbkdf2HmacSha256(
    password: String,
    salt: ByteArray,
    iterations: Int,
    keyLengthBytes: Int
): ByteArray

// Las contraseñas nunca se guardan en claro: se almacena "pbkdf2_sha256$<iteraciones>$<sal>$<hash>".
// Guardar las iteraciones en el propio valor permite subirlas en el futuro sin invalidar
// las cuentas existentes (needsRehash).
@OptIn(ExperimentalEncodingApi::class)
internal object PasswordHasher {

    private const val ALGORITHM = "pbkdf2_sha256"
    // Recomendación OWASP para PBKDF2-HMAC-SHA256.
    private const val ITERATIONS = 600_000
    private const val KEY_LENGTH_BYTES = 32

    fun hash(password: String): String {
        val salt = randomSalt()
        val hash = pbkdf2HmacSha256(password, salt, ITERATIONS, KEY_LENGTH_BYTES)
        return listOf(ALGORITHM, ITERATIONS.toString(), Base64.encode(salt), Base64.encode(hash))
            .joinToString("$")
    }

    fun verify(password: String, stored: String): Boolean {
        if (isLegacyPlainText(stored)) {
            // Cuentas creadas antes de introducir el hash: se aceptan una vez y el llamante
            // debe re-guardarlas con hash (needsRehash).
            return constantTimeEquals(password.encodeToByteArray(), stored.encodeToByteArray())
        }
        val parts = stored.split("$")
        if (parts.size != 4 || parts[0] != ALGORITHM) return false
        val iterations = parts[1].toIntOrNull()?.takeIf { it > 0 } ?: return false
        val salt = runCatching { Base64.decode(parts[2]) }.getOrNull() ?: return false
        val expected = runCatching { Base64.decode(parts[3]) }.getOrNull() ?: return false
        if (salt.isEmpty() || expected.isEmpty()) return false

        val actual = pbkdf2HmacSha256(password, salt, iterations, expected.size)
        return constantTimeEquals(actual, expected)
    }

    fun needsRehash(stored: String): Boolean =
        isLegacyPlainText(stored) || stored.split("$").getOrNull(1)?.toIntOrNull() != ITERATIONS

    private fun isLegacyPlainText(stored: String) = !stored.startsWith("$ALGORITHM$")

    // Uuid.random() usa el generador criptográficamente seguro de la plataforma
    // (122 bits aleatorios por UUID v4); dos UUID dan una sal de 32 bytes.
    @OptIn(ExperimentalUuidApi::class)
    private fun randomSalt(): ByteArray = Uuid.random().toByteArray() + Uuid.random().toByteArray()

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        var diff = a.size xor b.size
        for (i in 0 until minOf(a.size, b.size)) {
            diff = diff or (a[i].toInt() xor b[i].toInt())
        }
        return diff == 0
    }
}
