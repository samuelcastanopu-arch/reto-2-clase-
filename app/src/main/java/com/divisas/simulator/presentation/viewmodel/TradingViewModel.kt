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
import android.content.Context
import android.graphics.Bitmap
import com.divisas.simulator.service.ClientFaceRecognitionService
import com.divisas.simulator.service.FaceDetectionResult
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

    // Estado del Hardware de Cámara y Reconocimiento del Cliente
    private val _customerPhotoUri = MutableStateFlow<String?>(null)
    val customerPhotoUri: StateFlow<String?> = _customerPhotoUri.asStateFlow()

    private val _faceRecognitionVerified = MutableStateFlow(false)
    val faceRecognitionVerified: StateFlow<Boolean> = _faceRecognitionVerified.asStateFlow()

    private val _isAnalyzingPhoto = MutableStateFlow(false)
    val isAnalyzingPhoto: StateFlow<Boolean> = _isAnalyzingPhoto.asStateFlow()

    private val _recognitionDetails = MutableStateFlow<FaceDetectionResult.Success?>(null)
    val recognitionDetails: StateFlow<FaceDetectionResult.Success?> = _recognitionDetails.asStateFlow()

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

    /**
     * Procesa la foto tomada por el hardware de la cámara del cliente.
     * Guarda la imagen en almacenamiento local y ejecuta el reconocimiento facial biométrico con ML Kit.
     */
    fun processCustomerCapturedPhoto(context: Context, bitmap: Bitmap, currentUser: User?) {
        viewModelScope.launch {
            _isAnalyzingPhoto.value = true
            try {
                val savedUri = ClientFaceRecognitionService.saveCapturedPhoto(context, bitmap, prefix = "client_tx")
                _customerPhotoUri.value = savedUri

                ClientFaceRecognitionService.analyzeClientFace(bitmap, currentUser) { result ->
                    _isAnalyzingPhoto.value = false
                    when (result) {
                        is FaceDetectionResult.Success -> {
                            _faceRecognitionVerified.value = true
                            _recognitionDetails.value = result
                            _message.value = "✅ ${result.message}"
                        }
                        is FaceDetectionResult.Error -> {
                            _faceRecognitionVerified.value = false
                            _recognitionDetails.value = null
                            _message.value = "⚠️ ${result.reason}"
                        }
                    }
                }
            } catch (e: Exception) {
                _isAnalyzingPhoto.value = false
                _message.value = "Error al procesar la foto de la cámara: ${e.localizedMessage}"
            }
        }
    }

    fun clearCustomerPhoto() {
        _customerPhotoUri.value = null
        _faceRecognitionVerified.value = false
        _recognitionDetails.value = null
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

        // Validación de Reconocimiento de Cliente por Foto (Hardware)
        if (!_faceRecognitionVerified.value || _customerPhotoUri.value == null) {
            onError("📸 Se requiere capturar la foto del cliente con la cámara para el reconocimiento y autorización del cambio de divisas.")
            return
        }

        val result = transactionRepository.createTransaction(
            userId = currentUser.uid,
            userEmail = currentUser.email,
            userName = currentUser.fullName,
            type = _operationType.value,
            currencyCode = _selectedCurrency.value.code,
            amount = amount,
            rateAtOrder = appliedRate,
            customerPhotoUri = _customerPhotoUri.value,
            faceRecognitionVerified = _faceRecognitionVerified.value
        )

        if (result.isSuccess) {
            val op = result.getOrNull()!!
            loadUserData(currentUser)
            clearCustomerPhoto()
            _message.value = "Transacción autorizada con foto del cliente y enviada a auditoría."
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
