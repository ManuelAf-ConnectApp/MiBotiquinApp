package com.connectapp.data.interceptor

import com.connectapp.data.datasource.AuthLocalDataSource
import com.connectapp.domain.model.AppError

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
        } catch (e: AppError) {
            // AppError extiende Throwable (no Exception): errores de dominio ya tipados.
            Result.failure(e)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            
            // Map exceptions to domain AppError
            val appError = when (e) {
                // Example of future mapping for network libraries:
                // is KtorException -> AppError.NetworkError(e.message)
                else -> AppError.UnknownError(e.message)
            }

            Result.failure(appError)
        }
    }
}
