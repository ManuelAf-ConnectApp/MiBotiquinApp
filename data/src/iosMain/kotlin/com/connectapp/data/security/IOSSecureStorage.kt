package com.connectapp.data.security

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecNotAvailable
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.posix.memcpy

// Keychain (genericPassword). "AfterFirstUnlockThisDeviceOnly": legible en segundo plano tras el
// primer desbloqueo y nunca se restaura en otro iPhone desde una copia de seguridad.
@OptIn(ExperimentalForeignApi::class)
class IOSSecureStorage(
    private val service: String = "com.connectapp.mibotiquinapp.secure"
) : SecureStorage {

    override fun getString(key: String): String? = memScoped {
        val query = newQuery(key)
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)

        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        CFRelease(query)
        if (status != errSecSuccess) return@memScoped null

        val data = CFBridgingRelease(result.value) as? NSData ?: return@memScoped null
        data.toByteArray().decodeToString()
    }

    override fun putString(key: String, value: String) {
        remove(key)
        val query = newQuery(key)
        CFDictionaryAddValue(query, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
        addObject(query, kSecValueData, value.encodeToByteArray().toNSData())
        val status = SecItemAdd(query, null)
        CFRelease(query)
        if (status != errSecSuccess) println("Keychain: no se pudo guardar '$key' (status $status)")
    }

    override fun remove(key: String) {
        val query = newQuery(key)
        SecItemDelete(query)
        CFRelease(query)
    }

    /** false en procesos sin Keychain (p.ej. el binario de tests de Kotlin/Native en el simulador). */
    internal fun isKeychainAvailable(): Boolean {
        val query = newQuery("__availability_probe__")
        val status = SecItemDelete(query)
        CFRelease(query)
        return status != errSecNotAvailable
    }

    private fun newQuery(key: String): CFMutableDictionaryRef? {
        val query = CFDictionaryCreateMutable(
            null,
            0,
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr
        )
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        addObject(query, kSecAttrService, service)
        addObject(query, kSecAttrAccount, key)
        return query
    }

    // El diccionario retiene sus valores (kCFType callbacks): se libera la referencia propia.
    private fun addObject(dictionary: CFMutableDictionaryRef?, key: CFStringRef?, value: Any) {
        val ref = CFBridgingRetain(value)
        CFDictionaryAddValue(dictionary, key, ref)
        CFRelease(ref)
    }

    private fun ByteArray.toNSData(): NSData =
        if (isEmpty()) NSData() else usePinned { NSData.create(bytes = it.addressOf(0), length = size.convert()) }

    private fun NSData.toByteArray(): ByteArray {
        val size = length.toInt()
        if (size == 0) return ByteArray(0)
        return ByteArray(size).apply { usePinned { memcpy(it.addressOf(0), bytes, length) } }
    }
}
