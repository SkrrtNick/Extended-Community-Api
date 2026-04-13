package org.tribot.api.ge

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * Bulk price lookup using the OSRS Wiki Prices API.
 *
 * Prices are cached to a shared file on disk so that multiple TRiBot clients
 * (separate JVMs, one script each) share a single cache. When any client sees
 * the cache is stale it acquires a file lock, fetches all prices in one HTTP
 * call, and writes the result. Other clients waiting on the lock then read
 * the fresh file without making their own request.
 *
 * In-memory: each client also keeps a volatile in-memory snapshot so that
 * repeated lookups within the same tick don't hit disk.
 */
object PriceLookup {
    private const val BASE_URL = "https://prices.runescape.wiki/api/v1/osrs"
    // Wiki /latest CDN caches for 60s, RuneLite ItemManager refreshes every 30min.
    // 5 minutes is a good middle ground for active scripts.
    private const val DEFAULT_TTL_MS = 5 * 60 * 1000L
    private const val CACHE_FILE_NAME = "osrs-prices.json"

    @Volatile
    private var memoryCache: Map<Int, PriceEntry> = emptyMap()

    @Volatile
    private var lastMemoryLoad: Long = 0

    @Volatile
    private var ttlMs: Long = DEFAULT_TTL_MS

    private val gson = Gson()
    private val httpClient: HttpClient = HttpClient.newHttpClient()

    private val cacheDir: Path by lazy {
        val dir = Path.of(System.getProperty("user.home"), ".tribot", "cache", "prices")
        dir.toFile().mkdirs()
        dir
    }

    private val cacheFile: Path get() = cacheDir.resolve(CACHE_FILE_NAME)
    private val lockFile: Path get() = cacheDir.resolve("$CACHE_FILE_NAME.lock")

    /**
     * On-disk format: wraps the price map with a timestamp so readers can
     * check freshness without relying on file modification time.
     */
    private data class CacheEnvelope(
        val fetchTime: Long,
        val data: Map<String, PriceEntry>
    )

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
        ensureFresh()
        return memoryCache[itemId]
    }

    fun getBuyPrice(itemId: Int): Int? = getPrice(itemId)?.highPrice

    fun getSellPrice(itemId: Int): Int? = getPrice(itemId)?.lowPrice

    /**
     * Force-refresh: acquires the file lock, fetches from the API, writes to
     * disk, and loads into memory. If another process holds the lock we block
     * until it releases, then read the file it wrote.
     */
    fun refresh() {
        withFileLock {
            // Double-check: another process may have refreshed while we waited
            val existing = readCacheFile()
            if (existing != null && System.currentTimeMillis() - existing.fetchTime <= ttlMs) {
                loadIntoMemory(existing)
                return@withFileLock
            }

            // Fetch from API
            val newData = fetchFromApi() ?: return@withFileLock

            // Write to disk
            val envelope = CacheEnvelope(
                fetchTime = System.currentTimeMillis(),
                data = newData.mapKeys { it.key.toString() }
            )
            val json = gson.toJson(envelope)
            cacheFile.toFile().writeText(json)

            // Load into memory
            memoryCache = newData
            lastMemoryLoad = envelope.fetchTime
        }
    }

    fun invalidate() {
        memoryCache = emptyMap()
        lastMemoryLoad = 0
        try { cacheFile.toFile().delete() } catch (_: Exception) {}
    }

    /**
     * Ensures the in-memory cache is fresh. Checks in order:
     * 1. In-memory cache (fastest, no I/O)
     * 2. On-disk cache (another client may have refreshed)
     * 3. HTTP fetch (if disk is also stale)
     */
    private fun ensureFresh() {
        val now = System.currentTimeMillis()

        // Fast path: memory cache was loaded within the TTL window
        if (lastMemoryLoad > 0 && now - lastMemoryLoad <= ttlMs) return

        // Check disk cache (cheap I/O, no lock needed for reads)
        val diskCache = readCacheFile()
        if (diskCache != null && now - diskCache.fetchTime <= ttlMs) {
            loadIntoMemory(diskCache)
            return
        }

        // Disk is stale or missing — need to fetch
        refresh()
    }

    private fun loadIntoMemory(envelope: CacheEnvelope) {
        memoryCache = envelope.data.mapKeys { it.key.toInt() }
        lastMemoryLoad = envelope.fetchTime
    }

    private fun readCacheFile(): CacheEnvelope? {
        return try {
            val file = cacheFile.toFile()
            if (!file.exists() || file.length() == 0L) return null
            val json = file.readText()
            val type = object : TypeToken<CacheEnvelope>() {}.type
            gson.fromJson<CacheEnvelope>(json, type)
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchFromApi(): Map<Int, PriceEntry>? {
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$BASE_URL/latest"))
                .header("User-Agent", "osrs-pricing-client")
                .GET()
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) return null

            val type = object : TypeToken<Map<String, Map<String, Map<String, Number?>>>>() {}.type
            val parsed: Map<String, Map<String, Map<String, Number?>>> =
                gson.fromJson(response.body(), type)

            val data = parsed["data"] ?: return null

            data.entries.associate { (idStr, values) ->
                val id = idStr.toInt()
                val entry = PriceEntry(
                    highPrice = values["high"]?.toInt(),
                    lowPrice = values["low"]?.toInt(),
                    highTime = values["highTime"]?.toLong(),
                    lowTime = values["lowTime"]?.toLong()
                )
                id to entry
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Acquires an exclusive file lock across all JVMs on this machine,
     * executes [block], then releases the lock.
     */
    private fun withFileLock(block: () -> Unit) {
        try {
            FileChannel.open(
                lockFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
            ).use { channel ->
                val lock: FileLock = channel.lock() // blocks until acquired
                try {
                    block()
                } finally {
                    lock.release()
                }
            }
        } catch (_: Exception) {
            // If file locking fails (e.g. NFS), fall back to just running the block
            block()
        }
    }
}
