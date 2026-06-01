package com.canteen.presentation.user

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canteen.domain.model.MenuItem
import com.canteen.presentation.common.EmptyView
import com.canteen.presentation.common.LoadingView
import com.canteen.presentation.common.PrimaryActionButton
import com.canteen.utils.toRupeesText

@Composable
fun MenuScreen(
    viewModel: MenuViewModel,
    onOrderPlaced: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.orderPlaced) {
        if (uiState.orderPlaced) {
            onOrderPlaced()
        }
    }

    Scaffold(
        bottomBar = {
            if (uiState.cartCount > 0) {
                CartBar(
                    itemCount = uiState.cartCount,
                    totalText = uiState.totalCents.toRupeesText(),
                    isLoading = uiState.isPlacingOrder,
                    onPlaceOrder = viewModel::placeOrder
                )
            }
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
                    text = uiState.canteenName,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Choose items for pickup",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when {
                uiState.isLoading -> item { LoadingView() }
                uiState.error != null -> item {
                    EmptyView(
                        title = "Menu unavailable",
                        description = uiState.error.orEmpty()
                    )
                }
                uiState.menuItems.isEmpty() -> item {
                    EmptyView(
                        title = "No items yet",
                        description = "This canteen has not added menu items."
                    )
                }
                else -> items(uiState.menuItems, key = { it.id }) { item ->
                    MenuItemCard(
                        item = item,
                        quantity = uiState.cartQuantities[item.id] ?: 0,
                        onAdd = { viewModel.increment(item.id) },
                        onRemove = { viewModel.decrement(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuItemCard(
    item: MenuItem,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
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
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = item.priceCents.toRupeesText(),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (quantity == 0) {
                OutlinedButton(
                    onClick = onAdd,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Add")
                }
            } else {
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onRemove) {
                        Text("-")
                    }
                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(onClick = onAdd) {
                        Text("+")
                    }
                }
            }
        }
    }
}

@Composable
private fun CartBar(
    itemCount: Int,
    totalText: String,
    isLoading: Boolean,
    onPlaceOrder: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$itemCount item(s)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = totalText,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            PrimaryActionButton(
                text = "Place order",
                isLoading = isLoading,
                onClick = onPlaceOrder,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
