package com.connectapp.data.security

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CCKeyDerivationPBKDF
import platform.CoreCrypto.kCCPBKDF2
import platform.CoreCrypto.kCCPRFHmacAlgSHA256
import platform.CoreCrypto.kCCSuccess

@OptIn(ExperimentalForeignApi::class)
internal actual fun pbkdf2HmacSha256(
    password: String,
    salt: ByteArray,
    iterations: Int,
    keyLengthBytes: Int
): ByteArray {
    val derived = ByteArray(keyLengthBytes)

    // Kotlin/Native pasa el String como C string UTF-8; la longitud debe ser la de esos bytes.
    val status = salt.usePinned { s ->
        derived.usePinned { out ->
            CCKeyDerivationPBKDF(
                algorithm = kCCPBKDF2.convert(),
                password = password,
                passwordLen = password.encodeToByteArray().size.convert(),
                salt = s.addressOf(0).reinterpret(),
                saltLen = salt.size.convert(),
                prf = kCCPRFHmacAlgSHA256.convert(),
                rounds = iterations.convert(),
                derivedKey = out.addressOf(0).reinterpret(),
                derivedKeyLen = keyLengthBytes.convert()
            )
        }
    }
    check(status == kCCSuccess) { "CCKeyDerivationPBKDF failed with status $status" }
    return derived
}
