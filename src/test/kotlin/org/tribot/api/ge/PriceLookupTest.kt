package org.tribot.api.ge

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PriceLookupTest {

    @Test
    fun `PriceEntry data class holds correct values`() {
        val entry = PriceLookup.PriceEntry(
            highPrice = 1500000,
            lowPrice = 1480000,
            highTime = 1700000000L,
            lowTime = 1700000001L
        )
        assertEquals(1500000, entry.highPrice)
        assertEquals(1480000, entry.lowPrice)
        assertEquals(1700000000L, entry.highTime)
        assertEquals(1700000001L, entry.lowTime)
    }

    @Test
    fun `PriceEntry supports null values`() {
        val entry = PriceLookup.PriceEntry(
            highPrice = null,
            lowPrice = 500,
            highTime = null,
            lowTime = 1700000000L
        )
        assertNull(entry.highPrice)
        assertEquals(500, entry.lowPrice)
        assertNull(entry.highTime)
        assertEquals(1700000000L, entry.lowTime)
    }

    @Test
    fun `PriceEntry equality and copy work correctly`() {
        val entry1 = PriceLookup.PriceEntry(100, 90, 1000L, 1001L)
        val entry2 = PriceLookup.PriceEntry(100, 90, 1000L, 1001L)
        assertEquals(entry1, entry2)

        val entry3 = entry1.copy(highPrice = 200)
        assertEquals(200, entry3.highPrice)
        assertEquals(90, entry3.lowPrice)
    }

    @Test
    fun `setTtl does not throw`() {
        val original = 5 * 60 * 1000L
        PriceLookup.setTtl(10_000L)
        PriceLookup.setTtl(original)
    }
}
