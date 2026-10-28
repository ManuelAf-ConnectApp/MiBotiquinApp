package com.connectapp.data.datasource

import com.connectapp.data.model.UserDto
import kotlinx.coroutines.flow.Flow

interface UserLocalDataSource {
    suspend fun saveUser(user: UserDto)
    fun getUser(id: Long): Flow<UserDto?>
    suspend fun deleteUser(id: Long)
}
