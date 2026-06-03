package com.canteen.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.canteen.domain.model.UserRole
import com.canteen.presentation.common.CanteenTextField
import com.canteen.presentation.common.PrimaryActionButton

@Composable
fun AuthScreen(
    role: UserRole,
    viewModel: AuthViewModel,
    onAuthenticated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            viewModel.consumeAuthenticated()
            onAuthenticated()
        }
    }

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
                text = if (role == UserRole.USER) {
                    "Sign in as a student"
                } else {
                    "Sign in as a canteen owner"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            TabRow(selectedTabIndex = uiState.mode.ordinal) {
                Tab(
                    selected = uiState.mode == AuthMode.LOGIN,
                    onClick = { viewModel.onModeChange(AuthMode.LOGIN) },
                    text = { Text("Login") }
                )
                Tab(
                    selected = uiState.mode == AuthMode.REGISTER,
                    onClick = { viewModel.onModeChange(AuthMode.REGISTER) },
                    text = { Text("Register") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            CanteenTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email",
                keyboardType = KeyboardType.Email,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CanteenTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Password",
                isPassword = true,
                keyboardType = KeyboardType.Password,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryActionButton(
                text = if (uiState.mode == AuthMode.LOGIN) "Login" else "Create account",
                isLoading = uiState.isLoading,
                onClick = viewModel::submit
            )

            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
