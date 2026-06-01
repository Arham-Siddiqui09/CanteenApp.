package com.canteen.presentation.owner

import com.canteen.domain.model.Canteen
import com.canteen.domain.model.MenuItem
import com.canteen.domain.model.Order

data class OwnerHomeUiState(
    val canteen: Canteen? = null,
    val menuItems: List<MenuItem> = emptyList(),
    val orders: List<Order> = emptyList(),
    val editingItemId: String? = null,
    val itemName: String = "",
    val itemDescription: String = "",
    val itemPrice: String = "",
    val isLoading: Boolean = true,
    val isSavingItem: Boolean = false,
    val error: String? = null
)
