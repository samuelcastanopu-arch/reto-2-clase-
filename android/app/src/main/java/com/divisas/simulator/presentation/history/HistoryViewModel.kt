package com.divisas.simulator.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divisas.simulator.domain.model.Operation
import com.divisas.simulator.domain.model.OperationStatus
import com.divisas.simulator.domain.model.OperationType
import com.divisas.simulator.domain.model.UserRole
import com.divisas.simulator.domain.repository.AuthRepository
import com.divisas.simulator.domain.repository.OperationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = true,
    val allOperations: List<Operation> = emptyList(),
    val filteredOperations: List<Operation> = emptyList(),
    val statusFilter: OperationStatus? = null,
    val currencyFilter: String? = null,
    val typeFilter: OperationType? = null,
    val isAdmin: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val operationRepository: OperationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            val isAdmin = user?.role == UserRole.ADMIN
            _uiState.update { it.copy(isAdmin = isAdmin) }

            val flow = if (isAdmin) {
                operationRepository.getAllOperationsFlow()
            } else {
                operationRepository.getUserOperationsFlow(user?.uid ?: "")
            }

            flow.collect { ops ->
                _uiState.update { current ->
                    current.copy(
                        allOperations = ops,
                        isLoading = false
                    )
                }
                applyFilters()
            }
        }
    }

    fun onStatusFilterChange(status: OperationStatus?) {
        _uiState.update { it.copy(statusFilter = status) }
        applyFilters()
    }

    fun onCurrencyFilterChange(currency: String?) {
        _uiState.update { it.copy(currencyFilter = currency) }
        applyFilters()
    }

    fun onTypeFilterChange(type: OperationType?) {
        _uiState.update { it.copy(typeFilter = type) }
        applyFilters()
    }

    private fun applyFilters() {
        val current = _uiState.value
        var list = current.allOperations

        if (current.statusFilter != null) {
            list = list.filter { it.status == current.statusFilter }
        }
        if (!current.currencyFilter.isNullOrBlank()) {
            list = list.filter { it.currencyCode.equals(current.currencyFilter, ignoreCase = true) }
        }
        if (current.typeFilter != null) {
            list = list.filter { it.operationType == current.typeFilter }
        }

        _uiState.update { it.copy(filteredOperations = list) }
    }
}
