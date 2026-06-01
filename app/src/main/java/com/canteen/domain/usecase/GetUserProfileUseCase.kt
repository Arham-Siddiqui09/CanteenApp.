package com.canteen.domain.usecase

import com.canteen.domain.model.UserProfile
import com.canteen.domain.repository.CampusRepository

class GetUserProfileUseCase(
    private val repository: CampusRepository
) {
    suspend operator fun invoke(userId: String): Result<UserProfile?> =
        repository.getUserProfile(userId)
}
