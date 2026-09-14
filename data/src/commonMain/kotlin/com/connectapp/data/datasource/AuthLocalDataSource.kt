package com.connectapp.data.datasource

import com.connectapp.domain.manager.PreferenceStorage

interface AuthLocalDataSource {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearSession()
    fun saveRememberMe(remember: Boolean)
    fun isRememberMe(): Boolean
}

class AuthLocalDataSourceImpl(
    private val preferenceStorage: PreferenceStorage
) : AuthLocalDataSource {

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_REMEMBER_ME = "remember_me"
    }

    override fun saveToken(token: String) {
        preferenceStorage.putString(KEY_TOKEN, token)
    }

    override fun getToken(): String? {
        return preferenceStorage.getString(KEY_TOKEN)
    }

    override fun clearSession() {
        preferenceStorage.remove(KEY_TOKEN)
        preferenceStorage.putBoolean(KEY_REMEMBER_ME, false)
    }

    override fun saveRememberMe(remember: Boolean) {
        preferenceStorage.putBoolean(KEY_REMEMBER_ME, remember)
    }

    override fun isRememberMe(): Boolean {
        return preferenceStorage.getBoolean(KEY_REMEMBER_ME, false)
    }
}
