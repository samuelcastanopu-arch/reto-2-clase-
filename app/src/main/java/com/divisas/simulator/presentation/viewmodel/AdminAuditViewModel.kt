package com.divisas.simulator.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.divisas.simulator.data.repository.CurrencyRepository
import com.divisas.simulator.data.repository.TransactionRepository
import com.divisas.simulator.domain.model.Operation
import com.divisas.simulator.domain.model.OperationStatus
import com.divisas.simulator.service.FinnhubApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class AdminAuditViewModel(application: Application) : AndroidViewModel(application) {

    private val transactionRepository = TransactionRepository(application)
    private val currencyRepository = CurrencyRepository()

    private val _allTransactions = MutableStateFlow<List<Operation>>(emptyList())
    val allTransactions: StateFlow<List<Operation>> = _allTransactions.asStateFlow()

    private val _liveRatesAtTransition = MutableStateFlow<Map<String, Double>>(emptyMap())
    val liveRatesAtTransition: StateFlow<Map<String, Double>> = _liveRatesAtTransition.asStateFlow()

    private val _statusFilter = MutableStateFlow<OperationStatus?>(null) // null = todas
    val statusFilter: StateFlow<OperationStatus?> = _statusFilter.asStateFlow()

    private val _auditMessage = MutableStateFlow<String?>(null)
    val auditMessage: StateFlow<String?> = _auditMessage.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadTransactions()
        refreshLiveRates()
    }

    fun loadTransactions() {
        _allTransactions.value = transactionRepository.getAllTransactions()
    }

    fun setFilter(status: OperationStatus?) {
        _statusFilter.value = status
    }

    fun refreshLiveRates() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val map = mutableMapOf<String, Double>()
            listOf("USD", "EUR", "GBP", "JPY", "CAD").forEach { code ->
                val live = FinnhubApiService.fetchLiveRate(code)
                map[code] = live
            }
            _liveRatesAtTransition.value = map
            _isRefreshing.value = false
        }
    }

    fun getRateAtTransition(currencyCode: String, fallbackRate: Double): Double {
        return _liveRatesAtTransition.value[currencyCode.uppercase()]
            ?: FinnhubApiService.getCachedRate(currencyCode)
    }

    fun calculateDeviationAtTransition(rateAtOrder: Double, rateAtTransition: Double): Double {
        if (rateAtOrder <= 0.0) return 0.0
        val dev = abs(rateAtTransition - rateAtOrder) / rateAtOrder * 100.0
        return Math.round(dev * 100.0) / 100.0
    }

    fun auditTransaction(
        transactionId: String,
        adminName: String,
        rateAtTransition: Double,
        approve: Boolean,
        observation: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = transactionRepository.auditTransaction(
                transactionId = transactionId,
                adminName = adminName,
                rateAtTransition = rateAtTransition,
                approve = approve,
                observation = observation
            )

            if (result.isSuccess) {
                loadTransactions()
                val msg = if (approve) "Transacción aprobada como EXITOSA." else "Transacción RECHAZADA."
                _auditMessage.value = msg
                onResult(true, msg)
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Error al auditar."
                _auditMessage.value = err
                onResult(false, err)
            }
        }
    }

    fun clearAuditMessage() {
        _auditMessage.value = null
    }
}
