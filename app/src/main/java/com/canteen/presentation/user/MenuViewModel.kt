package com.canteen.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canteen.domain.model.Order
import com.canteen.domain.model.OrderItem
import com.canteen.domain.usecase.GetMenuItemsUseCase
import com.canteen.domain.usecase.PlaceOrderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MenuViewModel(
    canteenId: String,
    canteenName: String,
    private val getMenuItemsUseCase: GetMenuItemsUseCase,
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val currentUserId: () -> String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MenuUiState(canteenId = canteenId, canteenName = canteenName)
    )
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        loadMenu()
    }

    fun loadMenu() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(isLoading = true, error = null)
            _uiState.value = getMenuItemsUseCase(state.canteenId).fold(
                onSuccess = { items ->
                    state.copy(
                        menuItems = items.filter { it.isAvailable },
                        isLoading = false
                    )
                },
                onFailure = { exception ->
                    state.copy(
                        isLoading = false,
                        error = exception.message ?: "Could not load menu"
                    )
                }
            )
        }
    }

    fun increment(itemId: String) {
        val quantities = _uiState.value.cartQuantities.toMutableMap()
        quantities[itemId] = (quantities[itemId] ?: 0) + 1
        _uiState.value = _uiState.value.copy(cartQuantities = quantities)
    }

    fun decrement(itemId: String) {
        val quantities = _uiState.value.cartQuantities.toMutableMap()
        val nextQuantity = (quantities[itemId] ?: 0) - 1
        if (nextQuantity <= 0) {
            quantities.remove(itemId)
        } else {
            quantities[itemId] = nextQuantity
        }
        _uiState.value = _uiState.value.copy(cartQuantities = quantities)
    }

    fun placeOrder() {
        val state = _uiState.value
        val userId = currentUserId()
        if (userId == null) {
            _uiState.value = state.copy(error = "Please login again")
            return
        }
        if (state.cartCount == 0) {
            _uiState.value = state.copy(error = "Add at least one item")
            return
        }

        val orderItems = state.menuItems.mapNotNull { item ->
            val quantity = state.cartQuantities[item.id] ?: return@mapNotNull null
            OrderItem(
                menuItemId = item.id,
                name = item.name,
                priceCents = item.priceCents,
                quantity = quantity
            )
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isPlacingOrder = true, error = null)
            val order = Order(
                userId = userId,
                canteenId = state.canteenId,
                canteenName = state.canteenName,
                items = orderItems,
                totalCents = orderItems.sumOf { it.totalCents }
            )

            _uiState.value = placeOrderUseCase(order).fold(
                onSuccess = {
                    _uiState.value.copy(
                        isPlacingOrder = false,
                        cartQuantities = emptyMap(),
                        orderPlaced = true
                    )
                },
                onFailure = { exception ->
                    _uiState.value.copy(
                        isPlacingOrder = false,
                        error = exception.message ?: "Could not place order"
                    )
                }
            )
        }
    }
}
