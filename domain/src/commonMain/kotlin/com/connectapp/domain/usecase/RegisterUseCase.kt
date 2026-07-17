package com.connectapp.domain.usecase

import com.connectapp.domain.model.User
import com.connectapp.domain.repository.AuthRepository

class RegisterUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<User> {
        return repository.register(name, email, password)
    }
}
