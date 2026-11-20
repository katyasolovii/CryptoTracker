package ua.edu.cointracker.presentation.ui

import java.math.BigDecimal
import java.util.Locale

object PriceFormatter {
    fun format(price: Double?): String {
        if (price == null) return "Немає даних"
        val value = if (price >= 1.0) {
            String.format(Locale.US, "%.2f", price)
        } else {
            BigDecimal.valueOf(price).stripTrailingZeros().toPlainString()
        }
        return "\$$value"
    }
}
