package com.divisas.simulator.data.model

import com.divisas.simulator.domain.model.AccountStatus
import com.divisas.simulator.domain.model.Currency
import com.divisas.simulator.domain.model.Operation
import com.divisas.simulator.domain.model.OperationStatus
import com.divisas.simulator.domain.model.OperationType
import com.divisas.simulator.domain.model.RateHistory
import com.divisas.simulator.domain.model.User
import com.divisas.simulator.domain.model.UserRole
import com.google.firebase.Timestamp
import java.math.BigDecimal

data class UserDto(
    val uid: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val documentType: String? = null,
    val documentNumber: String? = null,
    val phoneNumber: String = "",
    val birthDate: Timestamp? = null,
    val address: String? = null,
    val photoUrl: String? = null,
    val photoPublicId: String? = null,
    val role: String = "client",
    val accountStatus: String = "ACTIVE",
    val createdAt: Timestamp? = null
) {
    fun toDomain(): User {
        return User(
            uid = uid,
            email = email,
            firstName = firstName,
            lastName = lastName,
            documentType = documentType,
            documentNumber = documentNumber,
            phoneNumber = phoneNumber,
            birthDate = birthDate?.toDate()?.time,
            address = address,
            photoUrl = photoUrl,
            photoPublicId = photoPublicId,
            role = if (role.lowercase() == "admin") UserRole.ADMIN else UserRole.CLIENT,
            accountStatus = when (accountStatus.uppercase()) {
                "INACTIVE" -> AccountStatus.INACTIVE
                "BLOCKED" -> AccountStatus.BLOCKED
                else -> AccountStatus.ACTIVE
            },
            createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis()
        )
    }
}

data class CurrencyDto(
    val code: String = "",
    val name: String = "",
    val symbol: String = "",
    val isActive: Boolean = true,
    val baseRate: Double = 0.0,
    val updatedAt: Timestamp? = null,
    val updatedByAdminEmail: String = ""
) {
    fun toDomain(): Currency {
        return Currency(
            code = code,
            name = name,
            symbol = symbol,
            isActive = isActive,
            baseRate = BigDecimal.valueOf(baseRate),
            updatedAt = updatedAt?.toDate()?.time ?: System.currentTimeMillis(),
            updatedByAdminEmail = updatedByAdminEmail
        )
    }
}

data class OperationDto(
    val id: String = "",
    val idempotencyKey: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val operationType: String = "BUY",
    val currencyCode: String = "USD",
    val amount: Double = 0.0,
    val baseRate: Double = 0.0,
    val appliedRate: Double = 0.0,
    val percentageDeviation: Double = 0.0,
    val totalCop: Double = 0.0,
    val status: String = "APPROVED",
    val rejectionReason: String? = null,
    val createdAt: Timestamp? = null
) {
    fun toDomain(): Operation {
        return Operation(
            id = id,
            idempotencyKey = idempotencyKey,
            userId = userId,
            userEmail = userEmail,
            operationType = if (operationType.uppercase() == "SELL") OperationType.SELL else OperationType.BUY,
            currencyCode = currencyCode,
            amount = BigDecimal.valueOf(amount),
            baseRate = BigDecimal.valueOf(baseRate),
            appliedRate = BigDecimal.valueOf(appliedRate),
            percentageDeviation = BigDecimal.valueOf(percentageDeviation),
            totalCop = BigDecimal.valueOf(totalCop),
            status = if (status.uppercase() == "REJECTED") OperationStatus.REJECTED else OperationStatus.APPROVED,
            rejectionReason = rejectionReason,
            createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis()
        )
    }
}

data class RateHistoryDto(
    val id: String = "",
    val currencyCode: String = "",
    val previousRate: Double = 0.0,
    val newRate: Double = 0.0,
    val changedAt: Timestamp? = null,
    val adminId: String = "",
    val adminEmail: String = "",
    val reason: String = ""
) {
    fun toDomain(): RateHistory {
        return RateHistory(
            id = id,
            currencyCode = currencyCode,
            previousRate = BigDecimal.valueOf(previousRate),
            newRate = BigDecimal.valueOf(newRate),
            changedAt = changedAt?.toDate()?.time ?: System.currentTimeMillis(),
            adminId = adminId,
            adminEmail = adminEmail,
            reason = reason
        )
    }
}
