package org.tribot.api.data

import com.google.gson.Gson
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DropDatabaseTest {

    private val gson = Gson()
    private lateinit var tempDir: Path
    private lateinit var manager: OsrsDataManager
    private lateinit var db: DropDatabase

    @BeforeTest
    fun setup() {
        tempDir = Files.createTempDirectory("drop-db-test")

        val drops = listOf(
            DropEntry(
                monsterName = "Abyssal demon",
                itemName = "Abyssal whip",
                itemId = 4151,
                quantity = "1",
                rarity = 0.001953125,
                noted = false,
                rolls = 1
            ),
            DropEntry(
                monsterName = "Abyssal demon",
                itemName = "Coins",
                itemId = 995,
                quantity = "132-462",
                rarity = 0.03125,
                noted = false,
                rolls = 1
            ),
            DropEntry(
                monsterName = "Chicken",
                itemName = "Coins",
                itemId = 995,
                quantity = "1",
                rarity = 1.0,
                noted = false,
                rolls = 1
            )
        )

        val json = gson.toJson(drops)
        Files.writeString(tempDir.resolve("drops.json"), json)

        val hash = OsrsDataManager.sha256(json)
        val metadata = Metadata(
            version = 1,
            scrapedAt = "2025-01-15T12:00:00Z",
            files = mapOf("drops.json" to FileInfo(hash = hash, entries = 3))
        )
        Files.writeString(tempDir.resolve("metadata.json"), gson.toJson(metadata))

        manager = OsrsDataManager(
            cacheDir = tempDir,
            baseUrl = "http://localhost:1/nonexistent"
        )
        db = DropDatabase(manager)
    }

    @AfterTest
    fun cleanup() {
        if (::tempDir.isInitialized) {
            tempDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun `getDropsForMonster returns correct drops`() {
        val drops = db.getDropsForMonster("Abyssal demon")
        assertNotNull(drops)
        assertEquals(2, drops.size)
        assertTrue(drops.any { it.itemName == "Abyssal whip" })
        assertTrue(drops.any { it.itemName == "Coins" })
    }

    @Test
    fun `getDropsForMonster is case insensitive`() {
        val drops1 = db.getDropsForMonster("abyssal demon")
        val drops2 = db.getDropsForMonster("ABYSSAL DEMON")
        assertNotNull(drops1)
        assertNotNull(drops2)
        assertEquals(2, drops1.size)
        assertEquals(2, drops2.size)
    }

    @Test
    fun `getDropsForItem returns drops across monsters`() {
        val drops = db.getDropsForItem("Coins")
        assertNotNull(drops)
        assertEquals(2, drops.size)
        assertTrue(drops.any { it.monsterName == "Abyssal demon" })
        assertTrue(drops.any { it.monsterName == "Chicken" })
    }

    @Test
    fun `getDropsForItem is case insensitive`() {
        val drops1 = db.getDropsForItem("coins")
        val drops2 = db.getDropsForItem("COINS")
        assertNotNull(drops1)
        assertNotNull(drops2)
        assertEquals(2, drops1.size)
        assertEquals(2, drops2.size)
    }

    @Test
    fun `getAll returns all drops`() {
        val all = db.getAll()
        assertEquals(3, all.size)
    }

    @Test
    fun `getDropsForMonster returns empty for unknown monster`() {
        val drops = db.getDropsForMonster("Nonexistent monster")
        assertTrue(drops.isEmpty())
    }
}
