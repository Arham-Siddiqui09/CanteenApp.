package com.canteen.domain.usecase

import com.canteen.domain.model.Order
import com.canteen.domain.repository.CampusRepository

class PlaceOrderUseCase(
    private val repository: CampusRepository
) {
    suspend operator fun invoke(order: Order): Result<Unit> =
        repository.placeOrder(order)
}
