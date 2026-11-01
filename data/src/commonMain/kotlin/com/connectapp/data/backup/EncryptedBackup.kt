package com.connectapp.data.backup

import dev.whyoleg.cryptography.BinarySize.Companion.bits
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.PBKDF2
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.random.CryptographyRandom
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Portable encrypted envelope for an offline backup. The account password, recovery hash and
 * session are intentionally not part of the plaintext supplied to this codec.
 */
data class EncryptedBackup(
    val format: String = FORMAT,
    val kdf: String = KDF,
    val iterations: Int = ITERATIONS,
    val salt: String,
    val ciphertext: String
) {
    companion object {
        const val FORMAT = "mibotiquin-backup-v1"
        const val KDF = "PBKDF2-HMAC-SHA256"
        const val ITERATIONS = 600_000
    }
}

class BackupCipher(private val json: Json = Json { ignoreUnknownKeys = false }) {
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun encrypt(plaintext: ByteArray, passphrase: CharArray): String {
        require(passphrase.size >= 12) { "The backup passphrase must have at least 12 characters." }
        try {
            val salt = CryptographyRandom.nextBytes(16)
            val key = derive(passphrase, salt)
            val ciphertext = key.cipher().encrypt(
                plaintext = plaintext,
                associatedData = EncryptedBackup.FORMAT.encodeToByteArray()
            )
            return json.encodeToString(buildJsonObject {
                put("format", EncryptedBackup.FORMAT)
                put("kdf", EncryptedBackup.KDF)
                put("iterations", EncryptedBackup.ITERATIONS)
                put("salt", Base64.encode(salt))
                put("ciphertext", Base64.encode(ciphertext))
            })
        } finally {
            passphrase.fill('\u0000')
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun decrypt(envelope: String, passphrase: CharArray): ByteArray {
        val objectValue = json.parseToJsonElement(envelope).jsonObject
        val parsed = EncryptedBackup(
            format = objectValue.getValue("format").jsonPrimitive.content,
            kdf = objectValue.getValue("kdf").jsonPrimitive.content,
            iterations = objectValue.getValue("iterations").jsonPrimitive.int,
            salt = objectValue.getValue("salt").jsonPrimitive.content,
            ciphertext = objectValue.getValue("ciphertext").jsonPrimitive.content
        )
        require(parsed.format == EncryptedBackup.FORMAT && parsed.kdf == EncryptedBackup.KDF) { "Unsupported backup format." }
        require(parsed.iterations == EncryptedBackup.ITERATIONS) { "Unsupported backup KDF parameters." }
        require(passphrase.size >= 12) { "The backup passphrase must have at least 12 characters." }
        return try {
            derive(passphrase, Base64.decode(parsed.salt)).cipher().decrypt(
                ciphertext = Base64.decode(parsed.ciphertext),
                associatedData = EncryptedBackup.FORMAT.encodeToByteArray()
            )
        } finally {
            passphrase.fill('\u0000')
        }
    }

    private suspend fun derive(passphrase: CharArray, salt: ByteArray): AES.GCM.Key {
        val bytes = CryptographyProvider.Default.get(PBKDF2).secretDerivation(
            digest = SHA256,
            iterations = EncryptedBackup.ITERATIONS,
            outputSize = 256.bits,
            salt = salt
        ).deriveSecretToByteArray(passphrase.concatToString().encodeToByteArray())
        return CryptographyProvider.Default.get(AES.GCM)
            .keyDecoder()
            .decodeFromByteArray(AES.Key.Format.RAW, bytes)
    }
}
