package com.connectapp.data.datasource.impl

import com.connectapp.data.database.dao.UserDao
import com.connectapp.data.datasource.UserLocalDataSource
import com.connectapp.data.mapper.toDto
import com.connectapp.data.mapper.toEntity
import com.connectapp.data.model.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserLocalDataSourceImpl(
    private val userDao: UserDao
) : UserLocalDataSource {
    override suspend fun saveUser(user: UserDto) {
        userDao.insertUser(user.toEntity())
    }

    override fun getUser(): Flow<UserDto?> {
        return userDao.getUser().map { it?.toDto() }
    }

    override suspend fun clearUser() {
        userDao.clearUser()
    }
}
