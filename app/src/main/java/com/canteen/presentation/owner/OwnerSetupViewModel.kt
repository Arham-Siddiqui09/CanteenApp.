package com.canteen.presentation.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canteen.domain.model.Canteen
import com.canteen.domain.usecase.GetCollegesUseCase
import com.canteen.domain.usecase.SaveCanteenUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class OwnerSetupViewModel(
    getCollegesUseCase: GetCollegesUseCase,
    private val saveCanteenUseCase: SaveCanteenUseCase,
    private val currentUserId: () -> String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OwnerSetupUiState(colleges = getCollegesUseCase())
    )
    val uiState: StateFlow<OwnerSetupUiState> = _uiState.asStateFlow()

    fun onCanteenNameChange(name: String) {
        _uiState.value = _uiState.value.copy(canteenName = name, error = null)
    }

    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(description = description, error = null)
    }

    fun onCollegeChange(college: String) {
        _uiState.value = _uiState.value.copy(college = college, error = null)
    }

    fun save() {
        val state = _uiState.value
        val ownerId = currentUserId()
        val validationError = when {
            ownerId == null -> "Please login again"
            state.canteenName.isBlank() -> "Canteen name is required"
            state.description.isBlank() -> "Description is required"
            state.college == "Select college" -> "Select a college"
            else -> null
        }

        if (validationError != null) {
            _uiState.value = state.copy(error = validationError)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val canteen = Canteen(
                ownerId = requireNotNull(ownerId),
                name = state.canteenName.trim(),
                description = state.description.trim(),
                college = state.college
            )

            val saveResult = runCatching {
                withTimeout(FIREBASE_TIMEOUT_MS) {
                    saveCanteenUseCase(canteen)
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
                                error = exception.message ?: "Could not save canteen"
                            )
                        }
                    )
                },
                onFailure = { exception ->
                    _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Saving canteen took too long. Check internet and Firestore rules."
                    )
                }
            )
        }
    }

    private companion object {
        const val FIREBASE_TIMEOUT_MS = 60_000L
    }
}
