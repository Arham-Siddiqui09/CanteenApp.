package com.canteen.presentation.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canteen.domain.model.MenuItem
import com.canteen.domain.model.OrderStatus
import com.canteen.domain.usecase.DeleteMenuItemUseCase
import com.canteen.domain.usecase.GetCanteenOrdersUseCase
import com.canteen.domain.usecase.GetMenuItemsUseCase
import com.canteen.domain.usecase.GetOwnerCanteenUseCase
import com.canteen.domain.usecase.SaveMenuItemUseCase
import com.canteen.domain.usecase.UpdateOrderStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OwnerHomeViewModel(
    private val getOwnerCanteenUseCase: GetOwnerCanteenUseCase,
    private val getMenuItemsUseCase: GetMenuItemsUseCase,
    private val saveMenuItemUseCase: SaveMenuItemUseCase,
    private val deleteMenuItemUseCase: DeleteMenuItemUseCase,
    private val getCanteenOrdersUseCase: GetCanteenOrdersUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase,
    private val currentUserId: () -> String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerHomeUiState())
    val uiState: StateFlow<OwnerHomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        val ownerId = currentUserId()
        if (ownerId == null) {
            _uiState.value = OwnerHomeUiState(isLoading = false, error = "Please login again")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val canteen = getOwnerCanteenUseCase(ownerId).getOrElse { exception ->
                _uiState.value = OwnerHomeUiState(
                    isLoading = false,
                    error = exception.message ?: "Could not load canteen"
                )
                return@launch
            }

            if (canteen == null) {
                _uiState.value = OwnerHomeUiState(
                    isLoading = false,
                    error = "No canteen registered for this owner."
                )
                return@launch
            }

            val menuItems = getMenuItemsUseCase(canteen.id).getOrElse { emptyList() }
            val ordersResult = getCanteenOrdersUseCase(canteen.id)
            val orders = ordersResult.getOrElse { emptyList() }
            _uiState.value = OwnerHomeUiState(
                canteen = canteen,
                menuItems = menuItems,
                orders = orders,
                isLoading = false,
                error = ordersResult.exceptionOrNull()?.message
            )
        }
    }

    fun onItemNameChange(value: String) {
        _uiState.value = _uiState.value.copy(itemName = value, error = null)
    }

    fun onItemDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(itemDescription = value, error = null)
    }

    fun onItemPriceChange(value: String) {
        _uiState.value = _uiState.value.copy(itemPrice = value, error = null)
    }

    fun editItem(item: MenuItem) {
        _uiState.value = _uiState.value.copy(
            editingItemId = item.id,
            itemName = item.name,
            itemDescription = item.description,
            itemPrice = (item.priceCents / 100.0).toString(),
            error = null
        )
    }

    fun clearItemForm() {
        _uiState.value = _uiState.value.copy(
            editingItemId = null,
            itemName = "",
            itemDescription = "",
            itemPrice = "",
            error = null
        )
    }

    fun saveItem() {
        val state = _uiState.value
        val canteen = state.canteen
        val priceCents = parsePriceCents(state.itemPrice)
        val validationError = when {
            canteen == null -> "Register a canteen first"
            state.itemName.isBlank() -> "Item name is required"
            state.itemDescription.isBlank() -> "Description is required"
            priceCents == null || priceCents <= 0 -> "Enter a valid price"
            else -> null
        }

        if (validationError != null) {
            _uiState.value = state.copy(error = validationError)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSavingItem = true, error = null)
            val item = MenuItem(
                id = state.editingItemId.orEmpty(),
                canteenId = requireNotNull(canteen).id,
                name = state.itemName.trim(),
                description = state.itemDescription.trim(),
                priceCents = requireNotNull(priceCents)
            )

            saveMenuItemUseCase(item).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSavingItem = false)
                    clearItemForm()
                    refreshMenuItems()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isSavingItem = false,
                        error = exception.message ?: "Could not save item"
                    )
                }
            )
        }
    }

    fun deleteItem(itemId: String) {
        val canteenId = _uiState.value.canteen?.id ?: return
        viewModelScope.launch {
            deleteMenuItemUseCase(canteenId, itemId)
            refreshMenuItems()
        }
    }

    private fun refreshMenuItems() {
        val canteenId = _uiState.value.canteen?.id ?: return
        viewModelScope.launch {
            getMenuItemsUseCase(canteenId).onSuccess { items ->
                _uiState.value = _uiState.value.copy(menuItems = items, error = null)
            }
        }
    }

    fun advanceOrder(orderId: String, currentStatus: OrderStatus) {
        val nextStatus = when (currentStatus) {
            OrderStatus.PENDING -> OrderStatus.ACCEPTED
            OrderStatus.ACCEPTED -> OrderStatus.PREPARING
            OrderStatus.PREPARING -> OrderStatus.READY
            OrderStatus.READY -> OrderStatus.COMPLETED
            OrderStatus.COMPLETED -> OrderStatus.COMPLETED
        }

        viewModelScope.launch {
            updateOrderStatusUseCase(orderId, nextStatus)
            refreshOrders()
        }
    }

    private fun refreshOrders() {
        val canteenId = _uiState.value.canteen?.id ?: return
        viewModelScope.launch {
            getCanteenOrdersUseCase(canteenId).fold(
                onSuccess = { orders ->
                    _uiState.value = _uiState.value.copy(
                        orders = orders,
                        error = if (orders.isEmpty()) {
                            "No orders yet. Ask student to place order, then tap Refresh."
                        } else {
                            null
                        }
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Could not load orders from cloud."
                    )
                }
            )
        }
    }

    private fun parsePriceCents(price: String): Int? =
        price.toDoubleOrNull()?.let { (it * 100).toInt() }
}
