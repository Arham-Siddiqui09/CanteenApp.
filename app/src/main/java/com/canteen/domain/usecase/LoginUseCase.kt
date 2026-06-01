package com.canteen.domain.usecase

import com.canteen.domain.model.AuthUser
import com.canteen.domain.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<AuthUser> =
        repository.login(email.trim(), password)
}
