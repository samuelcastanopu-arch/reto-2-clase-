package com.divisas.simulator.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divisas.simulator.core.result.Resource
import com.divisas.simulator.domain.model.User
import com.divisas.simulator.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email.trim(), errorMessage = null) }
    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password, errorMessage = null) }
    fun onConfirmPasswordChange(confirm: String) = _uiState.update { it.copy(confirmPassword = confirm, errorMessage = null) }
    fun onFirstNameChange(name: String) = _uiState.update { it.copy(firstName = name, errorMessage = null) }
    fun onLastNameChange(lastName: String) = _uiState.update { it.copy(lastName = lastName, errorMessage = null) }
    fun onPhoneChange(phone: String) = _uiState.update { it.copy(phoneNumber = phone, errorMessage = null) }

    fun login() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese correo y contraseña.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = authRepository.login(email, password)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true, user = result.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun register() {
        val state = _uiState.value
        if (state.firstName.isBlank() || state.lastName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese su nombre y apellido.") }
            return
        }
        if (state.email.isBlank() || !state.email.contains("@")) {
            _uiState.update { it.copy(errorMessage = "Ingrese un correo electrónico válido.") }
            return
        }
        if (state.phoneNumber.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingrese un número de teléfono.") }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres.") }
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Las contraseñas no coinciden.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = authRepository.register(
                email = state.email,
                pass = state.password,
                firstName = state.firstName,
                lastName = state.lastName,
                phone = state.phoneNumber
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true, user = result.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                Resource.Loading -> Unit
            }
        }
    }
}
