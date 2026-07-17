package com.connectapp.domain.model

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
){
    init {
        require(firstName.isNotBlank()) { "First name cannot be blank" }
        require(lastName.isNotBlank()) { "Last name cannot be blank" }
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(phone.isNotBlank()) { "Phone cannot be blank" }
    }
}
