package com.connectapp.data.security

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

internal actual fun pbkdf2HmacSha256(
    password: String,
    salt: ByteArray,
    iterations: Int,
    keyLengthBytes: Int
): ByteArray {
    val spec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLengthBytes * 8)
    return try {
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    } finally {
        spec.clearPassword()
    }
}
