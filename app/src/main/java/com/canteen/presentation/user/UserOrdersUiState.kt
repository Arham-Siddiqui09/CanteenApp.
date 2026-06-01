package com.canteen.presentation.user

import com.canteen.domain.model.Order

data class UserOrdersUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
