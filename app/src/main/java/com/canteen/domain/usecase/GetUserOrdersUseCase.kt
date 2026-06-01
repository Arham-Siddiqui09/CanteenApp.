package com.canteen.domain.usecase

import com.canteen.domain.model.Order
import com.canteen.domain.repository.CampusRepository

class GetUserOrdersUseCase(
    private val repository: CampusRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Order>> =
        repository.getOrdersForUser(userId)
}
