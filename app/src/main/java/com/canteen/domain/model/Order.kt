package com.canteen.domain.model

data class Order(
    val id: String = "",
    val userId: String,
    val canteenId: String,
    val canteenName: String,
    val items: List<OrderItem>,
    val totalCents: Int,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)
