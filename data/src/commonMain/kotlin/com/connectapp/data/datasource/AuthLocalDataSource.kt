package com.connectapp.data.datasource

interface AuthLocalDataSource {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearSession()
}

class AuthLocalDataSourceImpl : AuthLocalDataSource {
    private var cachedToken: String? = null // Simulation of local storage (e.g., DataStore or SharedPreferences)

    override fun saveToken(token: String) {
        cachedToken = token
    }

    override fun getToken(): String? {
        return cachedToken
    }

    override fun clearSession() {
        cachedToken = null
    }
}
