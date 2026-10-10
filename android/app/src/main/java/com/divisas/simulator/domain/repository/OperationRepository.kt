package com.divisas.simulator.domain.repository

import com.divisas.simulator.core.result.Resource
import com.divisas.simulator.domain.model.Operation
import com.divisas.simulator.domain.model.OperationType
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface OperationRepository {
    suspend fun simulateOperation(
        currencyCode: String,
        operationType: OperationType,
        amount: BigDecimal,
        appliedRate: BigDecimal
    ): Resource<Operation>

    fun getUserOperationsFlow(userId: String): Flow<List<Operation>>
    fun getAllOperationsFlow(): Flow<List<Operation>>
}
