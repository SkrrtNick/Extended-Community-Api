package org.tribot.api.ge

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration test that hits the real OSRS Wiki API.
 * Run with: ./gradlew test --tests "org.tribot.api.ge.PriceLookupIntegrationTest"
 */
class PriceLookupIntegrationTest {

    @Test
    fun `fetch real prices and verify well-known items`() {
        // Force a fresh fetch
        PriceLookup.invalidate()
        PriceLookup.setTtl(5 * 60 * 1000L)

        val start = System.currentTimeMillis()
        val whip = PriceLookup.getPrice(4151) // Abyssal whip
        val fetchMs = System.currentTimeMillis() - start

        println("First fetch (HTTP + disk write) took ${fetchMs}ms")

        // Whip should have price data
        assertNotNull(whip, "Abyssal whip (4151) should have price data")
        assertNotNull(whip.highPrice, "Whip should have a buy price")
        assertNotNull(whip.lowPrice, "Whip should have a sell price")
        assertTrue(whip.highPrice!! > 0, "Whip buy price should be positive: ${whip.highPrice}")
        println("Abyssal whip: buy=${whip.highPrice}, sell=${whip.lowPrice}")

        // Check a few more items
        val items = mapOf(
            11832 to "Bandos chestplate",
            13441 to "Anglerfish",
            379 to "Lobster",
            556 to "Air rune",
            2 to "Cannonball"
        )
        for ((id, name) in items) {
            val price = PriceLookup.getPrice(id)
            assertNotNull(price, "$name ($id) should have price data")
            println("$name: buy=${price.highPrice}, sell=${price.lowPrice}")
        }

        // Second lookup should be instant (memory cache)
        val start2 = System.currentTimeMillis()
        val whip2 = PriceLookup.getPrice(4151)
        val memMs = System.currentTimeMillis() - start2
        println("Memory cache lookup took ${memMs}ms")
        assertTrue(memMs < 5, "Memory lookup should be < 5ms, was ${memMs}ms")

        // Verify disk cache was written
        val cacheFile = java.io.File(System.getProperty("user.home"), ".tribot/cache/prices/osrs-prices.json")
        assertTrue(cacheFile.exists(), "Cache file should exist at ${cacheFile.absolutePath}")
        assertTrue(cacheFile.length() > 1000, "Cache file should have data (${cacheFile.length()} bytes)")
        println("Cache file: ${cacheFile.absolutePath} (${cacheFile.length() / 1024}KB)")
    }
}
