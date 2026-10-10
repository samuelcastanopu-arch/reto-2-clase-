package com.divisas.simulator.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divisas.simulator.domain.model.Currency
import com.divisas.simulator.domain.model.Operation
import com.divisas.simulator.domain.model.User
import com.divisas.simulator.domain.model.UserRole
import com.divisas.simulator.domain.repository.AuthRepository
import com.divisas.simulator.domain.repository.CurrencyRepository
import com.divisas.simulator.domain.repository.OperationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val currentUser: User? = null,
    val currencies: List<Currency> = emptyList(),
    val recentOperations: List<Operation> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val currencyRepository: CurrencyRepository,
    private val operationRepository: OperationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            authRepository.getCurrentUserFlow().collect { user ->
                _uiState.update { it.copy(currentUser = user) }
                if (user != null) {
                    observeOperations(user.uid, user.role == UserRole.ADMIN)
                }
            }
        }

        viewModelScope.launch {
            currencyRepository.getCurrenciesFlow().collect { currencies ->
                _uiState.update { it.copy(currencies = currencies, isLoading = false) }
            }
        }
    }

    private fun observeOperations(userId: String, isAdmin: Boolean) {
        viewModelScope.launch {
            val flow = if (isAdmin) {
                operationRepository.getAllOperationsFlow()
            } else {
                operationRepository.getUserOperationsFlow(userId)
            }
            flow.collect { operations ->
                _uiState.update { it.copy(recentOperations = operations.take(3)) }
            }
        }
    }
}
