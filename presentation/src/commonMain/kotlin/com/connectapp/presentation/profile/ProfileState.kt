package com.connectapp.presentation.profile

import com.connectapp.domain.model.User

data class ProfileState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)
