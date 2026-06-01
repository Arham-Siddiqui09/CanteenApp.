package com.canteen.domain.model

data class OrderItem(
    val menuItemId: String,
    val name: String,
    val priceCents: Int,
    val quantity: Int
) {
    val totalCents: Int = priceCents * quantity
}
