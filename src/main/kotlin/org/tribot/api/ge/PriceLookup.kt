package org.tribot.api.ge

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Bulk price lookup using the OSRS Wiki Prices API.
 *
 * All item prices are fetched in a single HTTP call to the `/latest` endpoint
 * and cached in memory with a configurable TTL (default 5 minutes).
 */
object PriceLookup {
    private const val BASE_URL = "https://prices.runescape.wiki/api/v1/osrs"
    private const val DEFAULT_TTL_MS = 5 * 60 * 1000L // 5 minutes

    private var cache: Map<Int, PriceEntry> = emptyMap()
    private var lastFetchTime: Long = 0
    private var ttlMs: Long = DEFAULT_TTL_MS

    private val gson = Gson()
    private val client: HttpClient = HttpClient.newHttpClient()

    /**
     * Represents a single item's latest trade prices from the GE.
     *
     * @property highPrice instant-buy (high) price, or null if no recent trade
     * @property lowPrice  instant-sell (low) price, or null if no recent trade
     * @property highTime  unix timestamp of the last high trade, or null
     * @property lowTime   unix timestamp of the last low trade, or null
     */
    data class PriceEntry(
        val highPrice: Int?,
        val lowPrice: Int?,
        val highTime: Long?,
        val lowTime: Long?
    )

    /** Set the cache time-to-live in milliseconds. */
    fun setTtl(ttlMs: Long) {
        this.ttlMs = ttlMs
    }

    /**
     * Get the full [PriceEntry] for an item, refreshing the cache if stale.
     * Returns null if the item has no price data or if the fetch fails.
     */
    fun getPrice(itemId: Int): PriceEntry? {
        refreshIfStale()
        return cache[itemId]
    }

    /** Convenience: get the instant-buy (high) price for an item. */
    fun getBuyPrice(itemId: Int): Int? = getPrice(itemId)?.highPrice

    /** Convenience: get the instant-sell (low) price for an item. */
    fun getSellPrice(itemId: Int): Int? = getPrice(itemId)?.lowPrice

    /**
     * Force-refresh the cache by fetching all prices from the Wiki API.
     * On failure the existing cache is retained.
     */
    fun refresh() {
        try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$BASE_URL/latest"))
                .header("User-Agent", "tribot-community-api")
                .GET()
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) return

            val type = object : TypeToken<Map<String, Map<String, Map<String, Number?>>>>() {}.type
            val parsed: Map<String, Map<String, Map<String, Number?>>> = gson.fromJson(response.body(), type)

            val data = parsed["data"] ?: return

            cache = data.entries.associate { (idStr, values) ->
                val id = idStr.toInt()
                val entry = PriceEntry(
                    highPrice = values["high"]?.toInt(),
                    lowPrice = values["low"]?.toInt(),
                    highTime = values["highTime"]?.toLong(),
                    lowTime = values["lowTime"]?.toLong()
                )
                id to entry
            }
            lastFetchTime = System.currentTimeMillis()
        } catch (_: Exception) {
            // On any failure, keep the existing cache intact
        }
    }

    private fun refreshIfStale() {
        if (System.currentTimeMillis() - lastFetchTime > ttlMs) {
            refresh()
        }
    }

    /** Invalidate the cache, clearing all entries and resetting the fetch time. */
    fun invalidate() {
        cache = emptyMap()
        lastFetchTime = 0
    }
}
