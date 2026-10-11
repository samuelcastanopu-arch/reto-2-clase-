package com.divisas.simulator.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.divisas.simulator.data.repository.CurrencyRepository
import com.divisas.simulator.data.repository.TransactionRepository
import com.divisas.simulator.domain.model.Currency
import com.divisas.simulator.domain.model.Operation
import com.divisas.simulator.domain.model.OperationType
import com.divisas.simulator.domain.model.User
import com.divisas.simulator.domain.model.Wallet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    private val transactionRepository = TransactionRepository(application)
    private val currencyRepository = CurrencyRepository()

    val currencies: StateFlow<List<Currency>> = currencyRepository.currencies

    private val _wallet = MutableStateFlow<Wallet?>(null)
    val wallet: StateFlow<Wallet?> = _wallet.asStateFlow()

    private val _userTransactions = MutableStateFlow<List<Operation>>(emptyList())
    val userTransactions: StateFlow<List<Operation>> = _userTransactions.asStateFlow()

    private val _selectedCurrency = MutableStateFlow<Currency>(currencies.value.first())
    val selectedCurrency: StateFlow<Currency> = _selectedCurrency.asStateFlow()

    private val _operationType = MutableStateFlow(OperationType.COMPRA)
    val operationType: StateFlow<OperationType> = _operationType.asStateFlow()

    private val _amountInput = MutableStateFlow("100")
    val amountInput: StateFlow<String> = _amountInput.asStateFlow()

    private val _appliedRateInput = MutableStateFlow("")
    val appliedRateInput: StateFlow<String> = _appliedRateInput.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        // Inicializar tasa sugerida con la divisa por defecto
        val defaultCurr = currencies.value.first()
        _appliedRateInput.value = defaultCurr.baseRate.toString()
        refreshMarketRates()
    }

    fun loadUserData(user: User) {
        _wallet.value = transactionRepository.getWallet(user.uid)
        _userTransactions.value = transactionRepository.getTransactionsForUser(user.uid)
    }

    fun selectCurrency(currency: Currency) {
        _selectedCurrency.value = currency
        _appliedRateInput.value = currency.baseRate.toString()
    }

    fun setOperationType(type: OperationType) {
        _operationType.value = type
    }

    fun setAmount(amount: String) {
        _amountInput.value = amount
    }

    fun setAppliedRate(rate: String) {
        _appliedRateInput.value = rate
    }

    fun refreshMarketRates() {
        viewModelScope.launch {
            _isRefreshing.value = true
            currencyRepository.refreshRates()
            _isRefreshing.value = false

            // Actualizar referencia de la divisa seleccionada
            val currentCode = _selectedCurrency.value.code
            val updated = currencyRepository.getCurrency(currentCode)
            if (updated != null) {
                _selectedCurrency.value = updated
            }
        }
    }

    fun calculateDeviationPercentage(): Double {
        val base = _selectedCurrency.value.baseRate
        val applied = _appliedRateInput.value.toDoubleOrNull() ?: base
        if (base <= 0.0) return 0.0
        val dev = abs(applied - base) / base * 100.0
        return Math.round(dev * 100.0) / 100.0
    }

    fun isWithinTolerance(): Boolean {
        return calculateDeviationPercentage() <= 2.5
    }

    fun calculateTotalCop(): Double {
        val amount = _amountInput.value.toDoubleOrNull() ?: 0.0
        val rate = _appliedRateInput.value.toDoubleOrNull() ?: _selectedCurrency.value.baseRate
        return amount * rate
    }

    fun submitTransaction(
        currentUser: User,
        onSuccess: (Operation) -> Unit,
        onError: (String) -> Unit
    ) {
        val amount = _amountInput.value.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            onError("Por favor ingrese un monto válido mayor a 0.")
            return
        }

        val appliedRate = _appliedRateInput.value.toDoubleOrNull()
        if (appliedRate == null || appliedRate <= 0.0) {
            onError("Por favor ingrese una tasa válida.")
            return
        }

        val result = transactionRepository.createTransaction(
            userId = currentUser.uid,
            userEmail = currentUser.email,
            userName = currentUser.fullName,
            type = _operationType.value,
            currencyCode = _selectedCurrency.value.code,
            amount = amount,
            rateAtOrder = appliedRate
        )

        if (result.isSuccess) {
            val op = result.getOrNull()!!
            loadUserData(currentUser)
            _message.value = "Transacción enviada a auditoría con éxito."
            onSuccess(op)
        } else {
            val err = result.exceptionOrNull()?.localizedMessage ?: "Error al registrar la transacción."
            onError(err)
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
