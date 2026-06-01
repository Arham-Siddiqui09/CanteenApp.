package com.canteen.domain.usecase

import com.canteen.domain.repository.CampusRepository

class DeleteMenuItemUseCase(
    private val repository: CampusRepository
) {
    suspend operator fun invoke(canteenId: String, itemId: String): Result<Unit> =
        repository.deleteMenuItem(canteenId, itemId)
}
