package com.connectapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val createdAt: String,
    val updatedAt: String
)
