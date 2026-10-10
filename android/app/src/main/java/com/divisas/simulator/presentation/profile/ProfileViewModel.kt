package com.divisas.simulator.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divisas.simulator.core.result.Resource
import com.divisas.simulator.domain.model.User
import com.divisas.simulator.domain.repository.AuthRepository
import com.divisas.simulator.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isUploadingPhoto: Boolean = false,
    val isLoggingOut: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            authRepository.getCurrentUserFlow().collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    fun uploadPhoto(uri: Uri) {
        _uiState.update { it.copy(isUploadingPhoto = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            when (val result = mediaRepository.uploadProfilePhoto(uri)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            successMessage = "Fotografía actualizada exitosamente."
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            errorMessage = result.message
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        _uiState.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggingOut = false) }
            onLoggedOut()
        }
    }
}
