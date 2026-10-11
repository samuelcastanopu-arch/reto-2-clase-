package com.divisas.simulator.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.divisas.simulator.data.repository.MultimediaRepository
import com.divisas.simulator.data.repository.TransactionRepository
import com.divisas.simulator.domain.model.MultimediaAsset
import com.divisas.simulator.domain.model.Operation
import com.divisas.simulator.service.MultimediaGenerationService
import com.divisas.simulator.service.MultimediaServiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MultimediaViewModel(application: Application) : AndroidViewModel(application) {

    private val multimediaRepository = MultimediaRepository(application)
    private val transactionRepository = TransactionRepository(application)

    val serviceState: StateFlow<MultimediaServiceState> = MultimediaGenerationService.serviceState

    private val _assets = MutableStateFlow<List<MultimediaAsset>>(emptyList())
    val assets: StateFlow<List<MultimediaAsset>> = _assets.asStateFlow()

    private val _recentTransactions = MutableStateFlow<List<Operation>>(emptyList())
    val recentTransactions: StateFlow<List<Operation>> = _recentTransactions.asStateFlow()

    init {
        loadAssets()
        loadRecentTransactions()

        // Observar cuando el servicio termine para guardar el asset en el repositorio
        viewModelScope.launch {
            serviceState.collect { state ->
                val newAsset = state.lastAsset
                if (newAsset != null && _assets.value.none { it.id == newAsset.id }) {
                    multimediaRepository.saveAsset(newAsset)
                    loadAssets()
                }
            }
        }
    }

    fun loadAssets() {
        _assets.value = multimediaRepository.getAllAssets()
    }

    fun loadRecentTransactions() {
        _recentTransactions.value = transactionRepository.getAllTransactions()
    }

    fun triggerMultimediaGeneration(
        context: Context,
        txId: String,
        currencyCode: String,
        amount: Double,
        type: String
    ) {
        MultimediaGenerationService.startGeneration(
            context = context,
            transactionId = txId,
            currencyCode = currencyCode,
            amount = amount,
            type = type
        )
    }

    fun playConfirmationAudio() {
        MultimediaGenerationService.playMultimediaTone()
    }
}
