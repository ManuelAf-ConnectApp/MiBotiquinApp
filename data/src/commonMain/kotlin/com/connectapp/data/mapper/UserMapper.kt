package com.connectapp.data.mapper

import com.connectapp.data.database.entity.UserEntity
import com.connectapp.data.model.UserDto
import com.connectapp.domain.model.User

fun UserDto.toEntity(): UserEntity = UserEntity(
    id = id.toLongOrNull() ?: 0L,
    firstName = firstName,
    lastName = lastName,
    email = email,
    password = password,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserEntity.toDto(): UserDto = UserDto(
    id = id.toString(),
    firstName = firstName,
    lastName = lastName,
    email = email,
    password = password,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserDto.toDomain(): User = User(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email
)

fun UserEntity.toDomain(): User = User(
    id = id.toString(),
    firstName = firstName,
    lastName = lastName,
    email = email
)
