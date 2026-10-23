package com.connectapp.data.datasource

import com.connectapp.data.security.SecureStorage
import com.connectapp.domain.manager.PreferenceStorage

interface AuthLocalDataSource {
    fun saveToken(token: String)
    fun getToken(): String?
    fun saveCurrentUserId(id: Long)
    fun getCurrentUserId(): Long?
    fun clearSession()
    fun saveRememberMe(remember: Boolean)
    fun isRememberMe(): Boolean
}

// Token e id de usuario van cifrados (SecureStorage); "recordarme" no es sensible y sigue en
// PreferenceStorage.
class AuthLocalDataSourceImpl(
    private val preferenceStorage: PreferenceStorage,
    private val secureStorage: SecureStorage
) : AuthLocalDataSource {

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
    }

    override fun saveToken(token: String) {
        secureStorage.putString(KEY_TOKEN, token)
    }

    override fun getToken(): String? {
        return readSecure(KEY_TOKEN)
    }

    override fun saveCurrentUserId(id: Long) {
        secureStorage.putString(KEY_CURRENT_USER_ID, id.toString())
    }

    override fun getCurrentUserId(): Long? {
        return readSecure(KEY_CURRENT_USER_ID)?.toLongOrNull()
    }

    override fun clearSession() {
        secureStorage.remove(KEY_TOKEN)
        secureStorage.remove(KEY_CURRENT_USER_ID)
        preferenceStorage.remove(KEY_TOKEN)
        preferenceStorage.remove(KEY_CURRENT_USER_ID)
        preferenceStorage.putBoolean(KEY_REMEMBER_ME, false)
    }

    // Versiones anteriores guardaban estos valores en claro en PreferenceStorage: se mueven al
    // almacén cifrado la primera vez que se leen, para no cerrar la sesión al actualizar.
    private fun readSecure(key: String): String? {
        secureStorage.getString(key)?.let { return it }
        val legacy = preferenceStorage.getString(key) ?: return null
        secureStorage.putString(key, legacy)
        preferenceStorage.remove(key)
        return legacy
    }

    override fun saveRememberMe(remember: Boolean) {
        preferenceStorage.putBoolean(KEY_REMEMBER_ME, remember)
    }

    override fun isRememberMe(): Boolean {
        return preferenceStorage.getBoolean(KEY_REMEMBER_ME, false)
    }
}
