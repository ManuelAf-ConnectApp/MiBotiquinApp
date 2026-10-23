package com.connectapp.data.security

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IOSSecureStorageTest {

    private val storage = IOSSecureStorage(service = "com.connectapp.mibotiquinapp.secure.test")

    @AfterTest
    fun cleanUp() {
        storage.remove("token")
    }

    @Test
    fun storesOverwritesAndRemovesValues() {
        // `gradle iosSimulatorArm64Test` ejecuta un binario sin firmar ni contenedor de app, que no
        // tiene Keychain (errSecNotAvailable). En ese entorno no hay nada que comprobar.
        if (!storage.isKeychainAvailable()) {
            println("Keychain no disponible en este entorno de test: se omite IOSSecureStorageTest")
            return
        }

        assertNull(storage.getString("token"))

        storage.putString("token", "valor-ñ-1")
        assertEquals("valor-ñ-1", storage.getString("token"))

        storage.putString("token", "valor-2")
        assertEquals("valor-2", storage.getString("token"))

        storage.remove("token")
        assertNull(storage.getString("token"))
    }
}
