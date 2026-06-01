package com.canteen.presentation.owner

data class OwnerSetupUiState(
    val canteenName: String = "",
    val description: String = "",
    val college: String = "Select college",
    val colleges: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)
