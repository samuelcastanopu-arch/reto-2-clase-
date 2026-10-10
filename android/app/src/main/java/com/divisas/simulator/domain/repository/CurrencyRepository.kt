package com.divisas.simulator.domain.repository

import com.divisas.simulator.core.result.Resource
import com.divisas.simulator.domain.model.Currency
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    fun getCurrenciesFlow(): Flow<List<Currency>>
    suspend fun getCurrencyByCode(code: String): Resource<Currency>
}
