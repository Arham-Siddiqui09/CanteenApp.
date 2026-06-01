package com.canteen.domain.model

data class Canteen(
    val id: String = "",
    val ownerId: String,
    val name: String,
    val description: String,
    val college: String
)
