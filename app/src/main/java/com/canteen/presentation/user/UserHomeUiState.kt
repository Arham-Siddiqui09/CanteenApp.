package com.canteen.presentation.user

import com.canteen.domain.model.Canteen
import com.canteen.domain.model.Order
import com.canteen.domain.model.UserProfile

data class UserHomeUiState(
    val profile: UserProfile? = null,
    val canteens: List<Canteen> = emptyList(),
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
