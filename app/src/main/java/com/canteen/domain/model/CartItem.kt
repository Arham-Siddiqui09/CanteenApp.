package com.canteen.domain.model

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int
) {
    val totalCents: Int = menuItem.priceCents * quantity
}
