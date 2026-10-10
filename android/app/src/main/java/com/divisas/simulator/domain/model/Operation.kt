package com.divisas.simulator.domain.model

import java.math.BigDecimal

enum class OperationType {
    BUY,
    SELL
}

enum class OperationStatus {
    APPROVED,
    REJECTED
}

data class Operation(
    val id: String,
    val idempotencyKey: String,
    val userId: String,
    val userEmail: String,
    val operationType: OperationType,
    val currencyCode: String,
    val amount: BigDecimal,
    val baseRate: BigDecimal,
    val appliedRate: BigDecimal,
    val percentageDeviation: BigDecimal,
    val totalCop: BigDecimal,
    val status: OperationStatus,
    val rejectionReason: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
