package com.connectapp.presentation.profile

import com.connectapp.domain.model.User
import com.connectapp.presentation.util.UiText

data class ProfileState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: UiText? = null
)
