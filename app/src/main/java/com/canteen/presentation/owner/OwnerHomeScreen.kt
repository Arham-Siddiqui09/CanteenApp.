package com.canteen.presentation.owner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Tab
import androidx.compose.material3.ExperimentalMaterial3Api
import com.canteen.presentation.common.CanteenTopBar
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.canteen.domain.model.MenuItem
import com.canteen.domain.model.Order
import com.canteen.domain.model.OrderStatus
import com.canteen.presentation.common.CanteenTextField
import com.canteen.presentation.common.EmptyView
import com.canteen.presentation.common.LoadingView
import com.canteen.presentation.common.PrimaryActionButton
import com.canteen.utils.toRupeesText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(
    viewModel: OwnerHomeViewModel,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            CanteenTopBar(
                title = uiState.canteen?.name ?: "Owner dashboard",
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = uiState.canteen?.college ?: "Manage your canteen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.isLoading) {
                item { LoadingView() }
                return@LazyColumn
            }

            if (uiState.error != null && uiState.canteen == null) {
                item { EmptyView("Setup needed", uiState.error.orEmpty()) }
                return@LazyColumn
            }

            item {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Menu") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Orders") }
                    )
                }
            }

            if (selectedTab == 0) {
                item {
                    MenuItemForm(
                        uiState = uiState,
                        onNameChange = viewModel::onItemNameChange,
                        onDescriptionChange = viewModel::onItemDescriptionChange,
                        onPriceChange = viewModel::onItemPriceChange,
                        onSave = viewModel::saveItem,
                        onClear = viewModel::clearItemForm
                    )
                }

                if (uiState.menuItems.isEmpty()) {
                    item { EmptyView("No menu items", "Add your first item so students can order.") }
                } else {
                    items(uiState.menuItems, key = { it.id }) { item ->
                        OwnerMenuItemCard(
                            item = item,
                            onEdit = { viewModel.editItem(item) },
                            onDelete = { viewModel.deleteItem(item.id) }
                        )
                    }
                }
            } else {
                item {
                    OutlinedButton(
                        onClick = viewModel::load,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Refresh orders")
                    }
                }
                if (uiState.orders.isEmpty()) {
                    item { EmptyView("No orders yet", "Incoming student orders will appear here.") }
                } else {
                    items(uiState.orders, key = { it.id }) { order ->
                        OwnerOrderCard(
                            order = order,
                            onAdvance = { viewModel.advanceOrder(order.id, order.status) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItemForm(
    uiState: OwnerHomeUiState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (uiState.editingItemId == null) "Add menu item" else "Edit menu item",
                style = MaterialTheme.typography.titleMedium
            )
            CanteenTextField(
                value = uiState.itemName,
                onValueChange = onNameChange,
                label = "Food name",
                modifier = Modifier.fillMaxWidth()
            )
            CanteenTextField(
                value = uiState.itemDescription,
                onValueChange = onDescriptionChange,
                label = "Description",
                modifier = Modifier.fillMaxWidth()
            )
            CanteenTextField(
                value = uiState.itemPrice,
                onValueChange = onPriceChange,
                label = "Price",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryActionButton(
                    text = "Save item",
                    isLoading = uiState.isSavingItem,
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(onClick = onClear) {
                    Text("Clear")
                }
            }
            uiState.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun OwnerMenuItemCard(
    item: MenuItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(item.priceCents.toRupeesText(), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onEdit) {
                    Text("Edit")
                }
                OutlinedButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun OwnerOrderCard(
    order: Order,
    onAdvance: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = order.items.joinToString { "${it.quantity} x ${it.name}" },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = order.totalCents.toRupeesText(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.status.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (order.status != OrderStatus.COMPLETED) {
                    Button(onClick = onAdvance) {
                        Text("Next status")
                    }
                }
            }
        }
    }
}
