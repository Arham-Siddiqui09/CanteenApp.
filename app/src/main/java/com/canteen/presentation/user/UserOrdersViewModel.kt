package com.canteen.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canteen.domain.usecase.GetUserOrdersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserOrdersViewModel(
    private val getUserOrdersUseCase: GetUserOrdersUseCase,
    private val currentUserId: () -> String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserOrdersUiState())
    val uiState: StateFlow<UserOrdersUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        val userId = currentUserId()
        if (userId == null) {
            _uiState.value = UserOrdersUiState(isLoading = false, error = "Please login again")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            _uiState.value = getUserOrdersUseCase(userId).fold(
                onSuccess = { orders ->
                    UserOrdersUiState(orders = orders, isLoading = false)
                },
                onFailure = { exception ->
                    UserOrdersUiState(
                        isLoading = false,
                        error = exception.message ?: "Could not load orders"
                    )
                }
            )
        }
    }
}
