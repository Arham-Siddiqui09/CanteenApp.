package com.canteen.domain.usecase

import com.canteen.domain.model.UserProfile
import com.canteen.domain.repository.CampusRepository

class SaveUserProfileUseCase(
    private val repository: CampusRepository
) {
    suspend operator fun invoke(profile: UserProfile): Result<Unit> =
        repository.saveUserProfile(profile)
}
