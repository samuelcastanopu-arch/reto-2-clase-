package com.divisas.simulator.core.constants

import java.math.BigDecimal

object AppConstants {
    // Regla de negocio: Desviación máxima permitida de 2,5 %
    val MAX_ALLOWED_DEVIATION_PERCENTAGE = BigDecimal("2.50")

    // Colecciones de Cloud Firestore
    object Collections {
        const val USERS = "users"
        const val CURRENCIES = "currencies"
        const val RATE_HISTORY = "rate_history"
        const val OPERATIONS = "operations"
        const val AUDIT_LOGS = "audit_logs"
    }

    // Cloud Functions invocables
    object Functions {
        const val SIMULATE_OPERATION = "simulateOperation"
        const val UPDATE_BASE_RATE = "updateBaseRate"
        const val GET_CLOUDINARY_SIGNATURE = "getCloudinaryUploadSignature"
        const val ASSIGN_USER_ROLE = "assignUserRole"
    }

    // Divisas iniciales soportadas
    object Currencies {
        const val USD = "USD"
        const val EUR = "EUR"
        const val COP = "COP"
    }
}
