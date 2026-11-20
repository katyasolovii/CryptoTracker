package ua.edu.cointracker.presentation.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class PriceFormatterTest {
    @Test
    fun format_keepsSmallPriceVisible() {
        assertEquals("\$0.00000001", PriceFormatter.format(0.00000001))
    }

    @Test
    fun formatShowsMessageWhenPriceIsMissing() {
        assertEquals("Немає даних", PriceFormatter.format(null))
    }

    @Test
    fun formatShowsTwoDecimalsForWholeDollarPrice() {
        assertEquals("\$12.35", PriceFormatter.format(12.345))
    }
}
