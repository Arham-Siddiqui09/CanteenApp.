package com.canteen.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canteen.domain.usecase.LoginUseCase
import com.canteen.domain.usecase.RegisterUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())

    val uiState: StateFlow<AuthUiState> =
        _uiState.asStateFlow()

    fun onModeChange(mode: AuthMode) {
        _uiState.value = _uiState.value.copy(
            mode = mode,
            error = null
        )
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            error = null
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            error = null
        )
    }

    fun submit() {
        val state = _uiState.value
        val validationError = validate(state.email, state.password)
        if (validationError != null) {
            _uiState.value = state.copy(error = validationError)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)

            val result = when (state.mode) {
                AuthMode.LOGIN -> loginUseCase(state.email, state.password)
                AuthMode.REGISTER -> registerUseCase(state.email, state.password)
            }

            _uiState.value = result.fold(
                onSuccess = {
                    _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Authentication failed"
                    )
                }
            )
        }
    }

    fun consumeAuthenticated() {
        _uiState.value = _uiState.value.copy(isAuthenticated = false)
    }

    private fun validate(email: String, password: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }
}
