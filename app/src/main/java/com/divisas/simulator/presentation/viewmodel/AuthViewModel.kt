package com.divisas.simulator.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.divisas.simulator.data.repository.UserRepository
import com.divisas.simulator.domain.model.User
import com.divisas.simulator.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository(application)

    private val _currentUser = MutableStateFlow<User?>(userRepository.getCurrentUser())
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit = {}) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Por favor ingrese correo y contraseña."
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = userRepository.login(email, password)
            _isLoading.value = false
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                onSuccess()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Error al iniciar sesión."
            }
        }
    }

    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        role: UserRole,
        onSuccess: () -> Unit = {}
    ) {
        if (email.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank()) {
            _errorMessage.value = "Todos los campos son obligatorios."
            return
        }
        if (password.length < 6) {
            _errorMessage.value = "La contraseña debe tener al menos 6 caracteres."
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = userRepository.register(email, password, firstName, lastName, role)
            _isLoading.value = false
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                onSuccess()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Error en el registro."
            }
        }
    }

    fun switchRoleForDemo(newRole: UserRole) {
        val updated = userRepository.switchRoleForTesting(newRole)
        _currentUser.value = updated
    }

    fun updateUserPhotoFromCamera(context: android.content.Context, bitmap: android.graphics.Bitmap) {
        val current = _currentUser.value ?: return
        try {
            val uri = com.divisas.simulator.service.ClientFaceRecognitionService.saveCapturedPhoto(context, bitmap, prefix = "profile_${current.uid}")
            val updated = userRepository.updateUserPhoto(current.uid, uri)
            if (updated != null) {
                _currentUser.value = updated
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error al guardar foto de perfil: ${e.localizedMessage}"
        }
    }

    fun logout(onSuccess: () -> Unit = {}) {
        userRepository.logout()
        _currentUser.value = null
        onSuccess()
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
