package com.divisas.simulator.domain.model

import java.math.BigDecimal

data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val isActive: Boolean = true,
    val baseRate: BigDecimal,
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedByAdminEmail: String = ""
)
