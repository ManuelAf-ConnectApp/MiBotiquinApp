package com.connectapp.data.interceptor

import com.connectapp.data.datasource.AuthLocalDataSource

interface NetworkInterceptor {
    fun getHeaders(): Map<String, String>
    suspend fun <T> execute(call: suspend () -> T): Result<T>
}

class AuthInterceptor(
    private val authLocalDataSource: AuthLocalDataSource
) : NetworkInterceptor {

    override fun getHeaders(): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        headers["Accept"] = "application/json"
        headers["Content-Type"] = "application/json"

        authLocalDataSource.getToken()?.let {
            headers["Authorization"] = "Bearer $it"
        }
        return headers
    }

    override suspend fun <T> execute(call: suspend () -> T): Result<T> {
        return try {
            Result.success(call())
        } catch (e: Exception) {
            // In the future, Ktor exceptions or exceptions from other libraries can be mapped
            // to domain errors here

            Result.failure(e)
        }
    }
}
