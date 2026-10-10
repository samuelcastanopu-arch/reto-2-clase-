package com.divisas.simulator.core.util

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val localeColombia = Locale("es", "CO")
    private val formatCop = NumberFormat.getCurrencyInstance(localeColombia).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    private val formatForeign = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    fun formatCop(amount: BigDecimal?): String {
        if (amount == null) return "$ 0 COP"
        return "${formatCop.format(amount)} COP"
    }

    fun formatCop(amount: Double?): String {
        return formatCop(amount?.let { BigDecimal.valueOf(it) })
    }

    fun formatForeign(amount: BigDecimal?, symbol: String = "$", code: String = "USD"): String {
        if (amount == null) return "$symbol 0.00 $code"
        return "$symbol ${formatForeign.format(amount)} $code"
    }

    fun formatForeign(amount: Double?, symbol: String = "$", code: String = "USD"): String {
        return formatForeign(amount?.let { BigDecimal.valueOf(it) }, symbol, code)
    }

    fun formatRate(rate: BigDecimal?): String {
        if (rate == null) return "$ 0,00"
        return "$ ${formatForeign.format(rate)} COP"
    }

    fun formatRate(rate: Double?): String {
        return formatRate(rate?.let { BigDecimal.valueOf(it) })
    }
}
