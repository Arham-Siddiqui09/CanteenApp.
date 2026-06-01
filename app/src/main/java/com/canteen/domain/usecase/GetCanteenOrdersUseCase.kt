package com.canteen.domain.usecase

import com.canteen.domain.model.Order
import com.canteen.domain.repository.CampusRepository

class GetCanteenOrdersUseCase(
    private val repository: CampusRepository
) {
    suspend operator fun invoke(canteenId: String): Result<List<Order>> =
        repository.getOrdersForCanteen(canteenId)
}
