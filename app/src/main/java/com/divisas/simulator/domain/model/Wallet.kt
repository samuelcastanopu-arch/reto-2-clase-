package com.divisas.simulator.domain.model

data class Wallet(
    val userId: String,
    val copBalance: Double = 25000000.0, // Saldo inicial en COP para simular
    val balances: Map<String, Double> = mapOf("USD" to 500.0, "EUR" to 300.0, "GBP" to 150.0)
) {
    fun getForeignBalance(currencyCode: String): Double {
        return balances[currencyCode.uppercase()] ?: 0.0
    }
}
