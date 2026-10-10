package com.divisas.simulator.domain.usecase

import com.divisas.simulator.core.util.MathUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class ValidateRateDeviationUseCaseTest {

    private lateinit var useCase: ValidateRateDeviationUseCase
    private val baseRate = BigDecimal("4000.00")

    @Before
    fun setUp() {
        useCase = ValidateRateDeviationUseCase()
    }

    @Test
    fun `exact base rate has 0 percent deviation and is valid`() {
        val result = useCase(baseRate = baseRate, appliedRate = BigDecimal("4000.00"))

        assertTrue("La tasa idéntica a la base debe ser válida", result.isValid)
        assertEquals(0, BigDecimal("0.0000").compareTo(result.deviationPercentage))
        assertNull(result.errorMessage)
    }

    @Test
    fun `upper boundary at exact 2_50 percent is valid`() {
        // 4000 * 1.025 = 4100.00
        val result = useCase(baseRate = baseRate, appliedRate = BigDecimal("4100.00"))

        assertTrue("La tasa con exactamente 2.50% de desviación debe ser aceptada", result.isValid)
        assertEquals(0, BigDecimal("2.5000").compareTo(result.deviationPercentage))
        assertNull(result.errorMessage)
    }

    @Test
    fun `upper edge case at 2_49 percent is valid`() {
        // 4000 * 1.0249 = 4099.60
        val result = useCase(baseRate = baseRate, appliedRate = BigDecimal("4099.60"))

        assertTrue("La tasa con 2.49% de desviación debe ser aceptada", result.isValid)
        assertEquals(0, BigDecimal("2.4900").compareTo(result.deviationPercentage))
        assertNull(result.errorMessage)
    }

    @Test
    fun `upper edge case at 2_51 percent is rejected`() {
        // 4000 * 1.0251 = 4100.40
        val result = useCase(baseRate = baseRate, appliedRate = BigDecimal("4100.40"))

        assertFalse("La tasa con 2.51% de desviación debe ser rechazada", result.isValid)
        assertEquals(0, BigDecimal("2.5100").compareTo(result.deviationPercentage))
        assertNotNull("Debe proveer un mensaje de error explicativo", result.errorMessage)
    }

    @Test
    fun `lower boundary at exact minus 2_50 percent is valid`() {
        // 4000 * 0.975 = 3900.00
        val result = useCase(baseRate = baseRate, appliedRate = BigDecimal("3900.00"))

        assertTrue("La tasa inferior con exactamente 2.50% de desviación debe ser aceptada", result.isValid)
        assertEquals(0, BigDecimal("2.5000").compareTo(result.deviationPercentage))
        assertNull(result.errorMessage)
    }

    @Test
    fun `lower edge case at minus 2_49 percent is valid`() {
        // 4000 * (1 - 0.0249) = 3900.40
        val result = useCase(baseRate = baseRate, appliedRate = BigDecimal("3900.40"))

        assertTrue("La tasa inferior con 2.49% de desviación debe ser aceptada", result.isValid)
        assertEquals(0, BigDecimal("2.4900").compareTo(result.deviationPercentage))
        assertNull(result.errorMessage)
    }

    @Test
    fun `lower edge case at minus 2_51 percent is rejected`() {
        // 4000 * (1 - 0.0251) = 3899.60
        val result = useCase(baseRate = baseRate, appliedRate = BigDecimal("3899.60"))

        assertFalse("La tasa inferior con 2.51% de desviación debe ser rechazada", result.isValid)
        assertEquals(0, BigDecimal("2.5100").compareTo(result.deviationPercentage))
        assertNotNull(result.errorMessage)
    }

    @Test
    fun `negative applied rate is rejected`() {
        val result = useCase(baseRate = baseRate, appliedRate = BigDecimal("-100.00"))

        assertFalse("Tasas negativas deben ser rechazadas", result.isValid)
        assertNotNull(result.errorMessage)
    }

    @Test
    fun `zero applied rate is rejected`() {
        val result = useCase(baseRate = baseRate, appliedRate = BigDecimal.ZERO)

        assertFalse("Tasa aplicada igual a cero debe ser rechazada", result.isValid)
        assertNotNull(result.errorMessage)
    }

    @Test
    fun `zero or negative base rate is invalid`() {
        val resultZero = useCase(baseRate = BigDecimal.ZERO, appliedRate = BigDecimal("4000.00"))
        val resultNegative = useCase(baseRate = BigDecimal("-1.00"), appliedRate = BigDecimal("4000.00"))

        assertFalse("Tasa base cero debe ser inválida", resultZero.isValid)
        assertFalse("Tasa base negativa debe ser inválida", resultNegative.isValid)
    }

    @Test
    fun `total COP calculation is correct for buy and sell`() {
        val amount = BigDecimal("150.00")
        val appliedRate = BigDecimal("4050.00")

        val total = MathUtil.calculateTotalCop(amount, appliedRate)

        // 150 * 4050 = 607500.00
        assertEquals(0, BigDecimal("607500.00").compareTo(total))
    }

    @Test
    fun `allowed range calculation is accurate`() {
        val (minRate, maxRate) = MathUtil.getAllowedRateRange(baseRate)

        assertEquals(0, BigDecimal("3900.00").compareTo(minRate))
        assertEquals(0, BigDecimal("4100.00").compareTo(maxRate))
    }
}
