package com.divisas.simulator.domain.repository

import com.divisas.simulator.core.result.Resource
import com.divisas.simulator.domain.model.AuditLog
import com.divisas.simulator.domain.model.RateHistory
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface AdminRepository {
    suspend fun updateBaseRate(
        currencyCode: String,
        newRate: BigDecimal,
        reason: String
    ): Resource<Unit>

    fun getRateHistoryFlow(currencyCode: String?): Flow<List<RateHistory>>
    fun getAuditLogsFlow(): Flow<List<AuditLog>>
}
