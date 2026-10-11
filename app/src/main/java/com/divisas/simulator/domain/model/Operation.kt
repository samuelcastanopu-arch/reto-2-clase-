package com.divisas.simulator.domain.model

enum class OperationType {
    COMPRA, // Compra divisa extranjera pagando COP
    VENTA   // Vende divisa extranjera recibiendo COP
}

enum class OperationStatus {
    PENDIENTE_AUDITORIA, // Creada por usuario, esperando auditoría
    EXITOSA,             // Aprobada por admin (desviación <= 2.5%)
    RECHAZADA            // Rechazada por superar 2.5% u otra objeción
}

data class Operation(
    val id: String,
    val userId: String,
    val userEmail: String,
    val userName: String,
    val operationType: OperationType,
    val currencyCode: String,
    val amount: Double,
    val rateAtOrder: Double,          // Tasa acordada al momento de la orden
    val totalCop: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val rateAtTransition: Double = rateAtOrder,     // Tasa de la divisa al momento de la transición
    val percentageDeviation: Double = 0.0,          // |rateAtTransition - rateAtOrder| / rateAtOrder * 100
    val status: OperationStatus = OperationStatus.PENDIENTE_AUDITORIA,
    val auditObservation: String = "",
    val auditedBy: String? = null,
    val auditedAt: Long? = null
)
