package com.connectapp.data.security

// Almacén cifrado para secretos de sesión (token, usuario actual): Android Keystore en Android
// y Keychain en iOS. PreferenceStorage queda para ajustes no sensibles (tema, notificaciones...).
// Ante cualquier error de lectura devuelve null: en el peor caso el usuario vuelve a iniciar sesión.
interface SecureStorage {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
}
