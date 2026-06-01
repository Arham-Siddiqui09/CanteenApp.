package com.canteen.utils

fun Int.toRupeesText(): String = "₹${this / 100}.${(this % 100).toString().padStart(2, '0')}"
