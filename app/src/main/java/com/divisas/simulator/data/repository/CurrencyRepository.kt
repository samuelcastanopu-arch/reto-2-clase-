package com.divisas.simulator.data.repository

import com.divisas.simulator.domain.model.Currency
import com.divisas.simulator.service.FinnhubApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CurrencyRepository {

    private val initialCurrencies = listOf(
        Currency("USD", "Dólar Estadounidense", "$", "🇺🇸", 4150.0, "BINANCE:BTCUSDT"),
        Currency("EUR", "Euro Europeo", "€", "🇪🇺", 4485.0, "BINANCE:EURUSDT"),
        Currency("GBP", "Libra Esterlina", "£", "🇬🇧", 5240.0, "BINANCE:GBPUSDT"),
        Currency("JPY", "Yen Japonés", "¥", "🇯🇵", 28.5, "OANDA:USD_JPY"),
        Currency("CAD", "Dólar Canadiense", "C$", "🇨🇦", 3050.0, "BINANCE:BTCUSDT")
    )

    private val _currencies = MutableStateFlow(initialCurrencies)
    val currencies: StateFlow<List<Currency>> = _currencies.asStateFlow()

    suspend fun refreshRates() {
        val current = _currencies.value
        val updated = current.map { c ->
            val liveRate = FinnhubApiService.fetchLiveRate(c.code)
            c.copy(baseRate = liveRate, lastUpdated = System.currentTimeMillis())
        }
        _currencies.value = updated
    }

    fun getCurrency(code: String): Currency? {
        return _currencies.value.find { it.code.equals(code, ignoreCase = true) }
    }
}
