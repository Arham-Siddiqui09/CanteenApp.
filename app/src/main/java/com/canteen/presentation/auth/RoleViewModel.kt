package com.canteen.presentation.auth

import androidx.lifecycle.ViewModel
import com.canteen.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RoleViewModel : ViewModel() {

    private val _selectedRole =
        MutableStateFlow<UserRole?>(null)

    val selectedRole: StateFlow<UserRole?> = _selectedRole.asStateFlow()

    fun selectRole(role: UserRole) {
        _selectedRole.value = role
    }
}
