package com.canteen.domain.usecase

import com.canteen.domain.model.Canteen
import com.canteen.domain.repository.CampusRepository

class GetOwnerCanteenUseCase(
    private val repository: CampusRepository
) {
    suspend operator fun invoke(ownerId: String): Result<Canteen?> =
        repository.getOwnerCanteen(ownerId)
}
