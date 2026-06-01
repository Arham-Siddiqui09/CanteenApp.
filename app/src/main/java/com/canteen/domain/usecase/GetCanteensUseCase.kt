package com.canteen.domain.usecase

import com.canteen.domain.model.Canteen
import com.canteen.domain.repository.CampusRepository

class GetCanteensUseCase(
    private val repository: CampusRepository
) {
    suspend operator fun invoke(college: String): Result<List<Canteen>> =
        repository.getCanteensByCollege(college)
}
