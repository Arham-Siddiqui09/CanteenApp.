package com.canteen.presentation.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canteen.domain.model.Order
import com.canteen.presentation.common.EmptyView
import com.canteen.presentation.common.LoadingView
import com.canteen.utils.toRupeesText

@Composable
fun UserOrdersScreen(
    viewModel: UserOrdersViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Your orders",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            when {
                uiState.isLoading -> item { LoadingView() }
                uiState.error != null -> item {
                    EmptyView("Could not load orders", uiState.error.orEmpty())
                }
                uiState.orders.isEmpty() -> item {
                    EmptyView("No orders yet", "Place your first canteen order from the home screen.")
                }
                else -> items(uiState.orders, key = { it.id }) { order ->
                    UserOrderCard(order = order)
                }
            }
        }
    }
}

@Composable
private fun UserOrderCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = order.canteenName,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = order.items.joinToString { "${it.quantity} x ${it.name}" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "${order.status.name.lowercase().replaceFirstChar { it.uppercase() }} · ${order.totalCents.toRupeesText()}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
