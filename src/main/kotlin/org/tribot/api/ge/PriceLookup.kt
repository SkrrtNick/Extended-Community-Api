package org.tribot.api.ge

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Bulk price lookup using the OSRS Wiki Prices API.
 *
 * All item prices are fetched in a single HTTP call to the `/latest` endpoint
 * and cached in memory with a configurable TTL (default 5 minutes).
 *
 * Thread-safe: multiple scripts/threads can read concurrently. When the cache
 * is stale, only one thread performs the HTTP fetch — others wait for it to
 * complete and then read from the refreshed cache.
 */
object PriceLookup {
    private const val BASE_URL = "https://prices.runescape.wiki/api/v1/osrs"
    private const val DEFAULT_TTL_MS = 5 * 60 * 1000L // 5 minutes

    /** Cache is replaced atomically (immutable map swap), safe for concurrent reads. */
    @Volatile
    private var cache: Map<Int, PriceEntry> = emptyMap()

    @Volatile
    private var lastFetchTime: Long = 0

    @Volatile
    private var ttlMs: Long = DEFAULT_TTL_MS

    /** Guards the refresh path so only one thread fetches at a time. */
    private val refreshLock = ReentrantLock()

    private val gson = Gson()
    private val httpClient: HttpClient = HttpClient.newHttpClient()

    data class PriceEntry(
        val highPrice: Int?,
        val lowPrice: Int?,
        val highTime: Long?,
        val lowTime: Long?
    )

    fun setTtl(ttlMs: Long) {
        this.ttlMs = ttlMs
    }

    fun getPrice(itemId: Int): PriceEntry? {
        refreshIfStale()
        return cache[itemId]
    }

    fun getBuyPrice(itemId: Int): Int? = getPrice(itemId)?.highPrice

    fun getSellPrice(itemId: Int): Int? = getPrice(itemId)?.lowPrice

    /**
     * Force-refresh the cache by fetching all prices from the Wiki API.
     * If another thread is already refreshing, this call blocks until that
     * refresh completes rather than issuing a duplicate request.
     * On failure the existing cache is retained.
     */
    fun refresh() {
        refreshLock.withLock {
            // Double-check: another thread may have refreshed while we waited
            if (System.currentTimeMillis() - lastFetchTime <= ttlMs) return

            try {
                val request = HttpRequest.newBuilder()
                    .uri(URI.create("$BASE_URL/latest"))
                    .header("User-Agent", "osrs-pricing-client")
                    .GET()
                    .build()

                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                if (response.statusCode() != 200) return

                val type = object : TypeToken<Map<String, Map<String, Map<String, Number?>>>>() {}.type
                val parsed: Map<String, Map<String, Map<String, Number?>>> =
                    gson.fromJson(response.body(), type)

                val data = parsed["data"] ?: return

                // Build the new cache as a local val, then swap atomically
                val newCache = data.entries.associate { (idStr, values) ->
                    val id = idStr.toInt()
                    val entry = PriceEntry(
                        highPrice = values["high"]?.toInt(),
                        lowPrice = values["low"]?.toInt(),
                        highTime = values["highTime"]?.toLong(),
                        lowTime = values["lowTime"]?.toLong()
                    )
                    id to entry
                }

                cache = newCache
                lastFetchTime = System.currentTimeMillis()
            } catch (_: Exception) {
                // On any failure, keep the existing cache intact
            }
        }
    }

    private fun refreshIfStale() {
        // Fast path: volatile read, no locking if cache is fresh
        if (System.currentTimeMillis() - lastFetchTime <= ttlMs) return
        refresh()
    }

    fun invalidate() {
        refreshLock.withLock {
            cache = emptyMap()
            lastFetchTime = 0
        }
    }
}
