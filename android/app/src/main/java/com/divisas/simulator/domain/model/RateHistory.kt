package com.divisas.simulator.domain.model

import java.math.BigDecimal

data class RateHistory(
    val id: String,
    val currencyCode: String,
    val previousRate: BigDecimal,
    val newRate: BigDecimal,
    val changedAt: Long = System.currentTimeMillis(),
    val adminId: String,
    val adminEmail: String,
    val reason: String
)

data class AuditLog(
    val id: String,
    val action: String,
    val performedBy: String,
    val performedByEmail: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: Map<String, Any> = emptyMap()
)
