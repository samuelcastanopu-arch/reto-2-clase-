package com.divisas.simulator.domain.usecase

import com.divisas.simulator.core.util.MathUtil
import java.math.BigDecimal
import javax.inject.Inject

data class ValidationResult(
    val isValid: Boolean,
    val deviationPercentage: BigDecimal,
    val minAllowedRate: BigDecimal,
    val maxAllowedRate: BigDecimal,
    val errorMessage: String? = null
)

class ValidateRateDeviationUseCase @Inject constructor() {

    operator fun invoke(baseRate: BigDecimal, appliedRate: BigDecimal): ValidationResult {
        if (baseRate.compareTo(BigDecimal.ZERO) <= 0) {
            return ValidationResult(
                isValid = false,
                deviationPercentage = BigDecimal.ZERO,
                minAllowedRate = BigDecimal.ZERO,
                maxAllowedRate = BigDecimal.ZERO,
                errorMessage = "La tasa base no es válida."
            )
        }

        if (appliedRate.compareTo(BigDecimal.ZERO) <= 0) {
            val (minRate, maxRate) = MathUtil.getAllowedRateRange(baseRate)
            return ValidationResult(
                isValid = false,
                deviationPercentage = BigDecimal.ZERO,
                minAllowedRate = minRate,
                maxAllowedRate = maxRate,
                errorMessage = "La tasa aplicada debe ser mayor a cero."
            )
        }

        val deviation = MathUtil.calculatePercentageDeviation(baseRate, appliedRate)
        val isWithinAllowed = MathUtil.isWithinAllowedDeviation(baseRate, appliedRate)
        val (minRate, maxRate) = MathUtil.getAllowedRateRange(baseRate)

        val error = if (!isWithinAllowed) {
            "La tasa aplicada presenta una desviación de ${deviation}%, superando el margen permitido del 2.5% ($minRate - $maxRate COP)."
        } else null

        return ValidationResult(
            isValid = isWithinAllowed,
            deviationPercentage = deviation,
            minAllowedRate = minRate,
            maxAllowedRate = maxRate,
            errorMessage = error
        )
    }
}
