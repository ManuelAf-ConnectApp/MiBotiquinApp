package com.connectapp.data.mapper

import com.connectapp.data.model.UserDto
import com.connectapp.domain.model.User


fun UserDto.toDomain(): User = User(
    firstName = this.firstName,
    lastName = this.lastName,
    email = this.email,
    phone = this.phone

)
