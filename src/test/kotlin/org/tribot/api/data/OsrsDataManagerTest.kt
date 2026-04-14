package org.tribot.api.data

import com.google.gson.Gson
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OsrsDataManagerTest {

    private val gson = Gson()
    private lateinit var tempDir: Path

    private fun createTempDir(): Path {
        tempDir = Files.createTempDirectory("osrs-data-test")
        return tempDir
    }

    @AfterTest
    fun cleanup() {
        if (::tempDir.isInitialized) {
            tempDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun `returns null when no disk cache and no network`() {
        val dir = createTempDir()
        // Use a base URL that will never resolve
        val manager = OsrsDataManager(
            cacheDir = dir,
            baseUrl = "http://localhost:1/nonexistent"
        )
        val result = manager.getFileContent("items.json")
        assertNull(result)
    }

    @Test
    fun `returns disk cache when file exists with matching hash`() {
        val dir = createTempDir()
        dir.toFile().mkdirs()

        val fileContent = """[{"id":1,"name":"Cannonball"}]"""
        val hash = OsrsDataManager.sha256(fileContent)

        // Write the data file to disk
        Files.writeString(dir.resolve("items.json"), fileContent)

        // Write metadata with matching hash
        val metadata = Metadata(
            version = 1,
            scrapedAt = "2025-01-15T12:00:00Z",
            files = mapOf("items.json" to FileInfo(hash = hash, entries = 1))
        )
        Files.writeString(dir.resolve("metadata.json"), gson.toJson(metadata))

        val manager = OsrsDataManager(
            cacheDir = dir,
            baseUrl = "http://localhost:1/nonexistent"
        )
        // Force metadata to be loaded from disk without network
        val result = manager.getFileContent("items.json")
        assertEquals(fileContent, result)
    }

    @Test
    fun `sha256 produces consistent hashes`() {
        val input = "hello world"
        val hash1 = OsrsDataManager.sha256(input)
        val hash2 = OsrsDataManager.sha256(input)
        assertEquals(hash1, hash2)
        assertTrue(hash1.startsWith("sha256:"))
    }

    @Test
    fun `sha256 produces different hashes for different input`() {
        val hash1 = OsrsDataManager.sha256("hello")
        val hash2 = OsrsDataManager.sha256("world")
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `readLocalHash returns null when file does not exist`() {
        val dir = createTempDir()
        val manager = OsrsDataManager(
            cacheDir = dir,
            baseUrl = "http://localhost:1/nonexistent"
        )
        val result = manager.readLocalHash("nonexistent.json")
        assertNull(result)
    }

    @Test
    fun `readLocalHash returns hash of existing file`() {
        val dir = createTempDir()
        dir.toFile().mkdirs()

        val content = """{"test": true}"""
        Files.writeString(dir.resolve("test.json"), content)

        val manager = OsrsDataManager(
            cacheDir = dir,
            baseUrl = "http://localhost:1/nonexistent"
        )
        val result = manager.readLocalHash("test.json")
        assertNotNull(result)
        assertEquals(OsrsDataManager.sha256(content), result)
    }

    @Test
    fun `loadMetadataFromDisk returns null when no metadata file`() {
        val dir = createTempDir()
        val manager = OsrsDataManager(
            cacheDir = dir,
            baseUrl = "http://localhost:1/nonexistent"
        )
        val result = manager.loadMetadataFromDisk()
        assertNull(result)
    }

    @Test
    fun `loadMetadataFromDisk parses valid metadata`() {
        val dir = createTempDir()
        dir.toFile().mkdirs()

        val metadata = Metadata(
            version = 2,
            scrapedAt = "2025-06-01T08:00:00Z",
            files = mapOf(
                "items.json" to FileInfo(hash = "sha256:abc123", entries = 5000),
                "monsters.json" to FileInfo(hash = "sha256:def456", entries = 3000)
            )
        )
        Files.writeString(dir.resolve("metadata.json"), gson.toJson(metadata))

        val manager = OsrsDataManager(
            cacheDir = dir,
            baseUrl = "http://localhost:1/nonexistent"
        )
        val result = manager.loadMetadataFromDisk()
        assertNotNull(result)
        assertEquals(2, result.version)
        assertEquals("2025-06-01T08:00:00Z", result.scrapedAt)
        assertEquals(2, result.files.size)
        assertEquals("sha256:abc123", result.files["items.json"]?.hash)
        assertEquals(5000, result.files["items.json"]?.entries)
        assertEquals("sha256:def456", result.files["monsters.json"]?.hash)
        assertEquals(3000, result.files["monsters.json"]?.entries)
    }
}
