package com.canteen.domain.usecase

import com.canteen.domain.model.OrderStatus
import com.canteen.domain.repository.CampusRepository

class UpdateOrderStatusUseCase(
    private val repository: CampusRepository
) {
    suspend operator fun invoke(orderId: String, status: OrderStatus): Result<Unit> =
        repository.updateOrderStatus(orderId, status)
}
