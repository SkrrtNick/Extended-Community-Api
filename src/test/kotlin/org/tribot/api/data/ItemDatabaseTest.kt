package org.tribot.api.data

import com.google.gson.Gson
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ItemDatabaseTest {

    private val gson = Gson()
    private lateinit var tempDir: Path
    private lateinit var manager: OsrsDataManager
    private lateinit var db: ItemDatabase

    @BeforeTest
    fun setup() {
        tempDir = Files.createTempDirectory("item-db-test")

        val whip = ItemDefinition(
            id = 4151,
            name = "Abyssal whip",
            members = true,
            tradeable = true,
            tradeableOnGe = true,
            stackable = false,
            cost = 120001,
            highAlch = 72000,
            lowAlch = 48000,
            buyLimit = 70,
            weight = 0.453,
            examine = "A weapon from the abyss.",
            questItem = false,
            equipment = null,
            weapon = null
        )
        val coins = ItemDefinition(
            id = 995,
            name = "Coins",
            members = false,
            tradeable = true,
            tradeableOnGe = false,
            stackable = true,
            cost = 1,
            highAlch = null,
            lowAlch = null,
            buyLimit = null,
            weight = null,
            examine = null,
            questItem = false,
            equipment = null,
            weapon = null
        )

        val itemsMap = mapOf(
            "4151" to whip,
            "995" to coins
        )
        val json = gson.toJson(itemsMap)
        Files.writeString(tempDir.resolve("items.json"), json)

        // Write metadata with matching hash so OsrsDataManager serves from disk
        val hash = OsrsDataManager.sha256(json)
        val metadata = Metadata(
            version = 1,
            scrapedAt = "2025-01-15T12:00:00Z",
            files = mapOf("items.json" to FileInfo(hash = hash, entries = 2))
        )
        Files.writeString(tempDir.resolve("metadata.json"), gson.toJson(metadata))

        manager = OsrsDataManager(
            cacheDir = tempDir,
            baseUrl = "http://localhost:1/nonexistent"
        )
        db = ItemDatabase(manager)
    }

    @AfterTest
    fun cleanup() {
        if (::tempDir.isInitialized) {
            tempDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun `get returns item by id`() {
        val item = db.get(4151)
        assertNotNull(item)
        assertEquals(4151, item.id)
        assertEquals("Abyssal whip", item.name)
        assertTrue(item.members)
    }

    @Test
    fun `get returns null for unknown id`() {
        val item = db.get(99999)
        assertNull(item)
    }

    @Test
    fun `getByName returns item case insensitively`() {
        val item1 = db.getByName("Abyssal whip")
        assertNotNull(item1)
        assertEquals(4151, item1.id)

        val item2 = db.getByName("abyssal WHIP")
        assertNotNull(item2)
        assertEquals(4151, item2.id)

        val item3 = db.getByName("COINS")
        assertNotNull(item3)
        assertEquals(995, item3.id)
    }

    @Test
    fun `getByName returns null for unknown name`() {
        val item = db.getByName("Nonexistent item")
        assertNull(item)
    }

    @Test
    fun `getAll returns all items`() {
        val all = db.getAll()
        assertEquals(2, all.size)
        val ids = all.map { it.id }.toSet()
        assertTrue(ids.contains(4151))
        assertTrue(ids.contains(995))
    }

    @Test
    fun `second get call uses memory cache`() {
        val first = db.get(4151)
        val second = db.get(4151)
        assertNotNull(first)
        assertNotNull(second)
        assertSame(first, second)
    }
}
