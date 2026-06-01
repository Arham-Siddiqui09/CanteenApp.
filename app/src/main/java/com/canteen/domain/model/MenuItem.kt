package com.canteen.domain.model

data class MenuItem(
    val id: String = "",
    val canteenId: String,
    val name: String,
    val description: String,
    val priceCents: Int,
    val isAvailable: Boolean = true
)
