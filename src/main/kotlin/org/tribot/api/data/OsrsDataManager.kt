package org.tribot.api.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.time.Duration

/**
 * Central cache coordinator for OSRS data files hosted on GitHub.
 *
 * Fetches data files with metadata-driven refresh: a `metadata.json` file
 * describes the expected SHA-256 hash for each data file. When the local
 * copy matches, it is served from disk. When it differs (or is missing),
 * the file is fetched from the remote repository and written to disk.
 *
 * Multi-JVM safety is achieved via FileChannel/FileLock, following the
 * same pattern as [org.tribot.api.ge.PriceLookup]. Network failures
 * degrade gracefully: stale disk cache is returned, or null if no cache
 * exists at all.
 *
 * All 8 database singletons (ItemDatabase, MonsterDatabase, etc.) call
 * [getFileContent] to obtain their backing JSON.
 */
class OsrsDataManager(
    private val cacheDir: Path = DEFAULT_CACHE_DIR,
    private val baseUrl: String = DEFAULT_BASE_URL
) {

    companion object {
        private val DEFAULT_CACHE_DIR: Path by lazy {
            val dir = Path.of(System.getProperty("user.home"), ".tribot", "cache", "osrs-data")
            dir.toFile().mkdirs()
            dir
        }

        private const val DEFAULT_BASE_URL =
            "https://raw.githubusercontent.com/SkrrtNick/osrs-data/main/data"

        private const val METADATA_CHECK_INTERVAL_MS = 3_600_000L // 1 hour

        /**
         * Compute a SHA-256 hash of the given [input] string.
         *
         * @return a string in the form `sha256:<hex-digest>`
         */
        fun sha256(input: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
            val hex = bytes.joinToString("") { "%02x".format(it) }
            return "sha256:$hex"
        }
    }

    private val gson = Gson()
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    @Volatile
    private var cachedMetadata: Metadata? = null

    @Volatile
    private var lastMetadataCheck: Long = 0

    init {
        cacheDir.toFile().mkdirs()
    }

    /**
     * Main entry point for database singletons. Returns the file content
     * for the given [fileName] (e.g. `"items.json"`), using the metadata-
     * driven cache strategy:
     *
     * 1. Refresh metadata if stale (>1 hr since last check)
     * 2. Compare expected hash from metadata vs local file hash
     * 3. If match -> return disk cache
     * 4. If mismatch -> acquire file lock, double-check, fetch, write
     * 5. If fetch fails -> return stale disk cache
     * 6. If no disk cache -> return null
     */
    fun getFileContent(fileName: String): String? {
        refreshMetadataIfStale()

        val metadata = cachedMetadata
        val expectedHash = metadata?.files?.get(fileName)?.hash

        // Read local file content
        val localContent = readLocalFile(fileName)
        val localHash = if (localContent != null) sha256(localContent) else null

        // If hashes match, return disk cache
        if (expectedHash != null && localHash != null && expectedHash == localHash) {
            return localContent
        }

        // Hash mismatch or missing — need to fetch
        return fetchWithLock(fileName, expectedHash, localContent)
    }

    /**
     * Force-refresh metadata from the network, bypassing the staleness check.
     */
    fun refreshMetadata() {
        try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$baseUrl/metadata.json"))
                .header("User-Agent", "osrs-data-client")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            lastMetadataCheck = System.currentTimeMillis()
            if (response.statusCode() == 200) {
                val type = object : TypeToken<Metadata>() {}.type
                val meta: Metadata = gson.fromJson(response.body(), type)
                cachedMetadata = meta

                // Persist metadata to disk for offline use
                try {
                    Files.writeString(cacheDir.resolve("metadata.json"), response.body())
                } catch (_: Exception) { }
            }
        } catch (_: Exception) {
            // Network failure — fall back to disk metadata
            if (cachedMetadata == null) {
                cachedMetadata = loadMetadataFromDisk()
            }
            lastMetadataCheck = System.currentTimeMillis()
        }
    }

    /**
     * Compute the SHA-256 hash of a local cached file.
     *
     * @return the hash string (e.g. `sha256:abcdef...`), or null if the file
     *         does not exist or cannot be read.
     */
    fun readLocalHash(fileName: String): String? {
        val content = readLocalFile(fileName) ?: return null
        return sha256(content)
    }

    /**
     * Parse `metadata.json` from the local cache directory.
     *
     * @return the parsed [Metadata], or null if the file does not exist or
     *         cannot be parsed.
     */
    fun loadMetadataFromDisk(): Metadata? {
        return try {
            val file = cacheDir.resolve("metadata.json").toFile()
            if (!file.exists() || file.length() == 0L) return null
            val json = file.readText()
            val type = object : TypeToken<Metadata>() {}.type
            gson.fromJson<Metadata>(json, type)
        } catch (_: Exception) {
            null
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun refreshMetadataIfStale() {
        val now = System.currentTimeMillis()
        if (lastMetadataCheck > 0 && now - lastMetadataCheck <= METADATA_CHECK_INTERVAL_MS) return

        // Try network refresh first
        refreshMetadata()

        // If still no metadata in memory, try disk
        if (cachedMetadata == null) {
            cachedMetadata = loadMetadataFromDisk()
            lastMetadataCheck = System.currentTimeMillis()
        }
    }

    private fun readLocalFile(fileName: String): String? {
        return try {
            val file = cacheDir.resolve(fileName).toFile()
            if (!file.exists() || file.length() == 0L) return null
            file.readText()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Acquires an exclusive file lock, double-checks the hash, then fetches
     * from the network if still needed. Falls back to stale disk cache on
     * network failure, or null if no disk cache exists.
     */
    private fun fetchWithLock(
        fileName: String,
        expectedHash: String?,
        staleContent: String?
    ): String? {
        return withFileLock(fileName) {
            // Double-check: another process may have fetched while we waited
            val localContent = readLocalFile(fileName)
            if (localContent != null) {
                val localHash = sha256(localContent)
                if (expectedHash != null && expectedHash == localHash) {
                    return@withFileLock localContent
                }
            }

            // Fetch from network
            val fetched = fetchFile(fileName)
            if (fetched != null) {
                // Write to disk
                try {
                    Files.writeString(cacheDir.resolve(fileName), fetched)
                } catch (_: Exception) { }
                return@withFileLock fetched
            }

            // Network failed — return stale disk cache or null
            staleContent ?: localContent
        }
    }

    private fun fetchFile(fileName: String): String? {
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$baseUrl/$fileName"))
                .header("User-Agent", "osrs-data-client")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) response.body() else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Acquires an exclusive file lock across all JVMs on this machine,
     * executes [block], then releases the lock. Follows the same pattern
     * as [org.tribot.api.ge.PriceLookup].
     */
    private fun <T> withFileLock(fileName: String, block: () -> T): T {
        val lockFile = cacheDir.resolve("$fileName.lock")
        try {
            FileChannel.open(
                lockFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
            ).use { channel ->
                val lock: FileLock = channel.lock() // blocks until acquired
                try {
                    return block()
                } finally {
                    lock.release()
                }
            }
        } catch (_: Exception) {
            // If file locking fails (e.g. NFS), fall back to just running the block
            return block()
        }
    }
}
