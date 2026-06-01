package com.canteen.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canteen.domain.usecase.GetCanteensUseCase
import com.canteen.domain.usecase.GetUserOrdersUseCase
import com.canteen.domain.usecase.GetUserProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserHomeViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getCanteensUseCase: GetCanteensUseCase,
    private val getUserOrdersUseCase: GetUserOrdersUseCase,
    private val currentUserId: () -> String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserHomeUiState())
    val uiState: StateFlow<UserHomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        val userId = currentUserId()
        if (userId == null) {
            _uiState.value = UserHomeUiState(isLoading = false, error = "Please login again")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val profile = getUserProfileUseCase(userId).getOrElse { exception ->
                _uiState.value = UserHomeUiState(
                    isLoading = false,
                    error = exception.message ?: "Could not load profile"
                )
                return@launch
            }

            if (profile == null) {
                _uiState.value = UserHomeUiState(
                    isLoading = false,
                    error = "Profile not found. Complete profile setup first."
                )
                return@launch
            }

            val canteens = getCanteensUseCase(profile.college).getOrElse { emptyList() }
            val orders = getUserOrdersUseCase(userId).getOrElse { emptyList() }

            _uiState.value = UserHomeUiState(
                profile = profile,
                canteens = canteens,
                orders = orders,
                isLoading = false
            )
        }
    }
}
