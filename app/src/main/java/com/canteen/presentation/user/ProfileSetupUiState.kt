package com.canteen.presentation.user

data class ProfileSetupUiState(
    val name: String = "",
    val college: String = "Select college",
    val colleges: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)
