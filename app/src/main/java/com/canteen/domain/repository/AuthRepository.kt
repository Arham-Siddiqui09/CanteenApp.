package com.canteen.domain.repository

import com.canteen.domain.model.AuthUser

interface AuthRepository {
    suspend fun register(email: String, password: String): Result<AuthUser>
    suspend fun login(email: String, password: String): Result<AuthUser>
    fun currentUserId(): String?
}
