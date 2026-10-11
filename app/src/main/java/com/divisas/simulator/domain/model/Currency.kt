package com.divisas.simulator.domain.model

data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val flagEmoji: String,
    val baseRate: Double,
    val finnhubSymbol: String,
    val lastUpdated: Long = System.currentTimeMillis()
)
