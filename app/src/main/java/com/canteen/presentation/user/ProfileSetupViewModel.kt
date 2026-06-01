package com.canteen.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canteen.domain.model.UserProfile
import com.canteen.domain.usecase.GetCollegesUseCase
import com.canteen.domain.usecase.SaveUserProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class ProfileSetupViewModel(
    getCollegesUseCase: GetCollegesUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase,
    private val currentUserId: () -> String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileSetupUiState(colleges = getCollegesUseCase())
    )
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }

    fun onCollegeChange(college: String) {
        _uiState.value = _uiState.value.copy(college = college, error = null)
    }

    fun save() {
        val state = _uiState.value
        val userId = currentUserId()
        val validationError = when {
            userId == null -> "Please login again"
            state.name.isBlank() -> "Name is required"
            state.college == "Select college" -> "Select your college"
            else -> null
        }

        if (validationError != null) {
            _uiState.value = state.copy(error = validationError)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val profile = UserProfile(
                userId = requireNotNull(userId),
                name = state.name.trim(),
                college = state.college
            )

            val saveResult = runCatching {
                withTimeout(FIREBASE_TIMEOUT_MS) {
                    saveUserProfileUseCase(profile)
                }
            }

            _uiState.value = saveResult.fold(
                onSuccess = { result ->
                    result.fold(
                        onSuccess = {
                            _uiState.value.copy(isLoading = false, isSaved = true)
                        },
                        onFailure = { exception ->
                            _uiState.value.copy(
                                isLoading = false,
                                error = exception.message ?: "Could not save profile"
                            )
                        }
                    )
                },
                onFailure = { exception ->
                    _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Saving profile took too long. Check internet and Firestore rules."
                    )
                }
            )
        }
    }

    private companion object {
        const val FIREBASE_TIMEOUT_MS = 15_000L
    }
}
