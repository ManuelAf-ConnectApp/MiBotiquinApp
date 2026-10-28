package com.connectapp.data.util

import android.content.Context
import android.content.SharedPreferences
import com.connectapp.domain.manager.PreferenceStorage

class AndroidPreferenceStorage(context: Context) : PreferenceStorage {
    private val prefs: SharedPreferences = context.getSharedPreferences("MiBotiquinApp_prefs", Context.MODE_PRIVATE)

    init {
        val legacy = context.getSharedPreferences("pharma_control_prefs", Context.MODE_PRIVATE)
        if (prefs.all.isEmpty() && legacy.all.isNotEmpty()) {
            prefs.edit().also { editor ->
                legacy.all.forEach { (key, value) -> when (value) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                } }
            }.apply()
        }
    }

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putString(key: String, value: String?) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        prefs.getBoolean(key, defaultValue)

    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}

actual fun createPreferenceStorage(context: Any?): PreferenceStorage {
    require(context is Context) { "Context must be provided for Android" }
    return AndroidPreferenceStorage(context)
}
