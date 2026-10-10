package com.divisas.simulator.presentation.operations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.divisas.simulator.core.result.Resource
import com.divisas.simulator.core.util.MathUtil
import com.divisas.simulator.domain.model.Currency
import com.divisas.simulator.domain.model.Operation
import com.divisas.simulator.domain.model.OperationType
import com.divisas.simulator.domain.repository.CurrencyRepository
import com.divisas.simulator.domain.usecase.SimulateOperationUseCase
import com.divisas.simulator.domain.usecase.ValidateRateDeviationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class OperationsUiState(
    val availableCurrencies: List<Currency> = emptyList(),
    val selectedCurrency: Currency? = null,
    val operationType: OperationType = OperationType.BUY,
    val amountInput: String = "100",
    val appliedRateInput: String = "",
    val deviationPercentage: BigDecimal = BigDecimal.ZERO,
    val isWithinAllowedRange: Boolean = true,
    val minAllowedRate: BigDecimal = BigDecimal.ZERO,
    val maxAllowedRate: BigDecimal = BigDecimal.ZERO,
    val estimatedTotalCop: BigDecimal = BigDecimal.ZERO,
    val isSubmitting: Boolean = false,
    val lastResult: Operation? = null,
    val showResultDialog: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class OperationsViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    private val validateRateDeviationUseCase: ValidateRateDeviationUseCase,
    private val simulateOperationUseCase: SimulateOperationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OperationsUiState())
    val uiState: StateFlow<OperationsUiState> = _uiState.asStateFlow()

    init {
        loadCurrencies()
    }

    private fun loadCurrencies() {
        viewModelScope.launch {
            currencyRepository.getCurrenciesFlow().collect { currencies ->
                _uiState.update { current ->
                    val selected = current.selectedCurrency ?: currencies.firstOrNull()
                    val rateInput = if (current.appliedRateInput.isBlank() && selected != null) {
                        selected.baseRate.toPlainString()
                    } else {
                        current.appliedRateInput
                    }
                    current.copy(
                        availableCurrencies = currencies,
                        selectedCurrency = selected,
                        appliedRateInput = rateInput
                    )
                }
                recalculate()
            }
        }
    }

    fun onSelectCurrency(currency: Currency) {
        _uiState.update {
            it.copy(
                selectedCurrency = currency,
                appliedRateInput = currency.baseRate.toPlainString()
            )
        }
        recalculate()
    }

    fun onSelectOperationType(type: OperationType) {
        _uiState.update { it.copy(operationType = type) }
        recalculate()
    }

    fun onAmountChange(amount: String) {
        val filtered = amount.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(amountInput = filtered) }
        recalculate()
    }

    fun onAppliedRateChange(rate: String) {
        val filtered = rate.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(appliedRateInput = filtered) }
        recalculate()
    }

    private fun recalculate() {
        val current = _uiState.value
        val baseRate = current.selectedCurrency?.baseRate ?: BigDecimal.ZERO
        val appliedRate = current.appliedRateInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val amount = current.amountInput.toBigDecimalOrNull() ?: BigDecimal.ZERO

        val validation = validateRateDeviationUseCase(baseRate, appliedRate)
        val total = MathUtil.calculateTotalCop(amount, appliedRate)

        _uiState.update {
            it.copy(
                deviationPercentage = validation.deviationPercentage,
                isWithinAllowedRange = validation.isValid,
                minAllowedRate = validation.minAllowedRate,
                maxAllowedRate = validation.maxAllowedRate,
                estimatedTotalCop = total,
                errorMessage = validation.errorMessage
            )
        }
    }

    fun simulateOperation() {
        val current = _uiState.value
        val currency = current.selectedCurrency ?: return
        val amount = current.amountInput.toBigDecimalOrNull() ?: return
        val appliedRate = current.appliedRateInput.toBigDecimalOrNull() ?: return

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            val result = simulateOperationUseCase(
                currencyCode = currency.code,
                operationType = current.operationType,
                amount = amount,
                appliedRate = appliedRate
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            lastResult = result.data,
                            showResultDialog = true
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.message
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun dismissResultDialog() {
        _uiState.update { it.copy(showResultDialog = false, lastResult = null) }
    }
}
