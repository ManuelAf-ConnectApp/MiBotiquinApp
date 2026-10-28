package com.connectapp.data.security

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidSecureStorageTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val storage = AndroidSecureStorage(context)

    @After
    fun cleanUp() {
        storage.remove("token")
    }

    @Test
    fun storesEncryptedOverwritesAndRemovesValues() {
        assertNull(storage.getString("token"))

        storage.putString("token", "valor-ñ-1")
        assertEquals("valor-ñ-1", storage.getString("token"))

        // En disco sólo queda el texto cifrado.
        val raw = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE).getString("token", null)
        assertFalse(raw!!.contains("valor"))

        storage.putString("token", "valor-2")
        assertEquals("valor-2", AndroidSecureStorage(context).getString("token"))

        storage.remove("token")
        assertNull(storage.getString("token"))
    }

    @Test
    fun corruptedValuesAreDiscarded() {
        context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
            .edit().putString("token", "no-es-base64-valido").commit()

        assertNull(storage.getString("token"))
    }
}
