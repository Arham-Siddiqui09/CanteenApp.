package com.canteen.domain.usecase

import com.canteen.domain.model.Canteen
import com.canteen.domain.repository.CampusRepository

class SaveCanteenUseCase(
    private val repository: CampusRepository
) {
    suspend operator fun invoke(canteen: Canteen): Result<Unit> =
        repository.saveCanteen(canteen)
}
