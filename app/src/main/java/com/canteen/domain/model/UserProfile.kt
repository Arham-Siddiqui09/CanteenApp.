package com.canteen.domain.model

data class UserProfile(
    val userId: String,
    val name: String,
    val college: String,
    val role: UserRole = UserRole.USER
)
