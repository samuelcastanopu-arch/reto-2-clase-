package com.divisas.simulator.domain.usecase

import com.divisas.simulator.core.result.Resource
import com.divisas.simulator.domain.model.Operation
import com.divisas.simulator.domain.model.OperationType
import com.divisas.simulator.domain.repository.OperationRepository
import java.math.BigDecimal
import javax.inject.Inject

class SimulateOperationUseCase @Inject constructor(
    private val operationRepository: OperationRepository
) {
    suspend operator fun invoke(
        currencyCode: String,
        operationType: OperationType,
        amount: BigDecimal,
        appliedRate: BigDecimal
    ): Resource<Operation> {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Resource.Error("La cantidad a simular debe ser estrictamente mayor a cero.")
        }
        if (appliedRate.compareTo(BigDecimal.ZERO) <= 0) {
            return Resource.Error("La tasa aplicada debe ser estrictamente mayor a cero.")
        }

        return operationRepository.simulateOperation(
            currencyCode = currencyCode,
            operationType = operationType,
            amount = amount,
            appliedRate = appliedRate
        )
    }
}
