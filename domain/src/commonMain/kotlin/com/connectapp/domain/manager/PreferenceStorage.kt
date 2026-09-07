package com.connectapp.domain.manager

interface PreferenceStorage {
    fun getString(key: String): String?
    fun putString(key: String, value: String?)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun remove(key: String)
}

// Shared keys so every reader/writer of a given preference (potentially from
// different ViewModels/screens) stays in sync on the exact same storage key.
object PreferenceKeys {
    const val NOTIFICATIONS_ENABLED = "notifications_enabled"
}
