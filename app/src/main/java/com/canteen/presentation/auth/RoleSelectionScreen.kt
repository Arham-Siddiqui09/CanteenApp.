package com.canteen.presentation.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.canteen.domain.model.UserRole
import com.canteen.presentation.common.PrimaryActionButton

@Composable
fun RoleSelectionScreen(
    viewModel: RoleViewModel,
    onContinue: (UserRole) -> Unit
) {
    val selectedRole by viewModel.selectedRole.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Campus Canteen",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Choose how you want to use the app.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            RoleCard(
                title = "Student",
                subtitle = "Browse canteens, order food, and track pickup.",
                selected = selectedRole == UserRole.USER,
                onClick = { viewModel.selectRole(UserRole.USER) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            RoleCard(
                title = "Canteen Owner",
                subtitle = "Manage menu items, incoming orders, and readiness.",
                selected = selectedRole == UserRole.OWNER,
                onClick = { viewModel.selectRole(UserRole.OWNER) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryActionButton(
                text = "Continue",
                isLoading = false,
                enabled = selectedRole != null,
                onClick = { selectedRole?.let(onContinue) }
            )
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tonalElevation by animateDpAsState(
        targetValue = if (selected) 8.dp else 1.dp,
        label = "roleCardElevation"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        label = "roleCardBorder"
    )

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = tonalElevation),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
