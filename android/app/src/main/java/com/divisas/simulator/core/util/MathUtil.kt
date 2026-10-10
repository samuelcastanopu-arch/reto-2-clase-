package com.divisas.simulator.core.util

import com.divisas.simulator.core.constants.AppConstants
import java.math.BigDecimal
import java.math.RoundingMode

object MathUtil {

    private val ONE_HUNDRED = BigDecimal("100")

    /**
     * Calcula la desviación porcentual según la fórmula:
     * Desviación porcentual = (|Tasa aplicada - Tasa base| / Tasa base) * 100
     */
    fun calculatePercentageDeviation(
        baseRate: BigDecimal,
        appliedRate: BigDecimal
    ): BigDecimal {
        if (baseRate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO
        }
        val diff = appliedRate.subtract(baseRate).abs()
        return diff
            .divide(baseRate, 8, RoundingMode.HALF_UP)
            .multiply(ONE_HUNDRED)
            .setScale(4, RoundingMode.HALF_UP)
    }

    /**
     * Verifica si la desviación se encuentra dentro del margen de 2,5 %
     */
    fun isWithinAllowedDeviation(
        baseRate: BigDecimal,
        appliedRate: BigDecimal
    ): Boolean {
        if (baseRate.compareTo(BigDecimal.ZERO) <= 0 || appliedRate.compareTo(BigDecimal.ZERO) <= 0) {
            return false
        }
        val deviation = calculatePercentageDeviation(baseRate, appliedRate)
        return deviation.compareTo(AppConstants.MAX_ALLOWED_DEVIATION_PERCENTAGE) <= 0
    }

    /**
     * Obtiene el rango permitido de tasa aplicada [mínimo, máximo] dado una tasa base:
     * mínimo = baseRate * (1 - 0.025) = baseRate * 0.975
     * máximo = baseRate * (1 + 0.025) = baseRate * 1.025
     */
    fun getAllowedRateRange(baseRate: BigDecimal): Pair<BigDecimal, BigDecimal> {
        val minMultiplier = BigDecimal("0.975")
        val maxMultiplier = BigDecimal("1.025")

        val minRate = baseRate.multiply(minMultiplier).setScale(2, RoundingMode.HALF_UP)
        val maxRate = baseRate.multiply(maxMultiplier).setScale(2, RoundingMode.HALF_UP)

        return Pair(minRate, maxRate)
    }

    /**
     * Calcula el total en COP según la cantidad y la tasa aplicada
     */
    fun calculateTotalCop(amount: BigDecimal, appliedRate: BigDecimal): BigDecimal {
        if (amount.compareTo(BigDecimal.ZERO) <= 0 || appliedRate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO
        }
        return amount.multiply(appliedRate).setScale(2, RoundingMode.HALF_UP)
    }
}
