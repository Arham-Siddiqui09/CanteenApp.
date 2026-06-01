package com.canteen.presentation.user

import com.canteen.domain.model.MenuItem

data class MenuUiState(
    val canteenId: String,
    val canteenName: String,
    val menuItems: List<MenuItem> = emptyList(),
    val cartQuantities: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val isPlacingOrder: Boolean = false,
    val error: String? = null,
    val orderPlaced: Boolean = false
) {
    val cartCount: Int = cartQuantities.values.sum()
    val totalCents: Int = menuItems.sumOf { item ->
        item.priceCents * (cartQuantities[item.id] ?: 0)
    }
}
