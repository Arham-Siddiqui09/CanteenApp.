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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canteen.domain.model.Canteen
import com.canteen.presentation.common.EmptyView
import com.canteen.presentation.common.LoadingView

@Composable
fun UserHomeScreen(
    viewModel: UserHomeViewModel,
    onOpenCanteen: (Canteen) -> Unit,
    onOpenOrders: () -> Unit
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hi ${uiState.profile?.name ?: "Student"}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = uiState.profile?.college ?: "Your college",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = onOpenOrders) {
                        Text("Orders")
                    }
                }
            }

            when {
                uiState.isLoading -> item { LoadingView() }
                uiState.error != null -> item {
                    EmptyView(
                        title = "Something went wrong",
                        description = uiState.error.orEmpty()
                    )
                }
                uiState.canteens.isEmpty() -> item {
                    EmptyView(
                        title = "No canteens yet",
                        description = "Ask your canteen owner to register for this college."
                    )
                }
                else -> items(uiState.canteens, key = { it.id }) { canteen ->
                    CanteenCard(
                        canteen = canteen,
                        onOpen = { onOpenCanteen(canteen) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CanteenCard(
    canteen: Canteen,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = canteen.name,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = canteen.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View menu")
            }
        }
    }
}
