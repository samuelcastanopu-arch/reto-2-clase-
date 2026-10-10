package com.divisas.simulator.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divisas.simulator.core.result.Resource
import com.divisas.simulator.domain.model.AuditLog
import com.divisas.simulator.domain.model.Currency
import com.divisas.simulator.domain.model.RateHistory
import com.divisas.simulator.domain.repository.AdminRepository
import com.divisas.simulator.domain.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class AdminUiState(
    val selectedTab: Int = 0,
    val currencies: List<Currency> = emptyList(),
    val selectedCurrency: Currency? = null,
    val newRateInput: String = "",
    val reasonInput: String = "",
    val isUpdating: Boolean = false,
    val rateHistory: List<RateHistory> = emptyList(),
    val auditLogs: List<AuditLog> = emptyList(),
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            currencyRepository.getCurrenciesFlow().collect { list ->
                _uiState.update { current ->
                    current.copy(
                        currencies = list,
                        selectedCurrency = current.selectedCurrency ?: list.firstOrNull()
                    )
                }
            }
        }

        viewModelScope.launch {
            adminRepository.getRateHistoryFlow(null).collect { history ->
                _uiState.update { it.copy(rateHistory = history) }
            }
        }

        viewModelScope.launch {
            adminRepository.getAuditLogsFlow().collect { logs ->
                _uiState.update { it.copy(auditLogs = logs) }
            }
        }
    }

    fun onTabSelected(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onCurrencySelected(currency: Currency) {
        _uiState.update { it.copy(selectedCurrency = currency, newRateInput = "") }
    }

    fun onNewRateChange(rate: String) {
        val filtered = rate.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(newRateInput = filtered) }
    }

    fun onReasonChange(reason: String) {
        _uiState.update { it.copy(reasonInput = reason) }
    }

    fun updateBaseRate() {
        val current = _uiState.value
        val currency = current.selectedCurrency ?: return
        val newRate = current.newRateInput.toBigDecimalOrNull() ?: return
        val reason = current.reasonInput.trim()

        if (newRate.compareTo(BigDecimal.ZERO) <= 0) {
            _uiState.update { it.copy(errorMessage = "La nueva tasa debe ser mayor a cero.") }
            return
        }
        if (reason.length < 5) {
            _uiState.update { it.copy(errorMessage = "Debe indicar un motivo de cambio de al menos 5 caracteres.") }
            return
        }

        _uiState.update { it.copy(isUpdating = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = adminRepository.updateBaseRate(
                currencyCode = currency.code,
                newRate = newRate,
                reason = reason
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            successMessage = "Tasa de ${currency.code} actualizada exitosamente a $newRate COP.",
                            newRateInput = "",
                            reasonInput = ""
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            errorMessage = result.message
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
