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
import kotlin.test.assertTrue

class MonsterDatabaseTest {

    private val gson = Gson()
    private lateinit var tempDir: Path
    private lateinit var manager: OsrsDataManager
    private lateinit var db: MonsterDatabase

    @BeforeTest
    fun setup() {
        tempDir = Files.createTempDirectory("monster-db-test")

        val abyssalDemon = MonsterDefinition(
            id = 415,
            name = "Abyssal demon",
            members = true,
            combatLevel = 124,
            hitpoints = 150,
            maxHit = "8",
            attackSpeed = 4,
            size = 1,
            attackLevel = 97,
            strengthLevel = 67,
            defenceLevel = 135,
            magicLevel = 1,
            rangedLevel = 1,
            attackStab = 0,
            attackSlash = 0,
            attackCrush = 0,
            attackMagic = 0,
            attackRanged = 0,
            defenceStab = 20,
            defenceSlash = 20,
            defenceCrush = 20,
            defenceMagic = 0,
            defenceRanged = 20,
            strengthBonus = 0,
            rangedStrengthBonus = 0,
            magicDamageBonus = 0,
            slayerLevel = 85,
            slayerXp = 150.0,
            slayerCategory = "Abyssal demons",
            assignedBy = "Nieve|Duradel",
            elementalWeakness = null,
            elementalWeaknessPercent = null,
            poisonous = "No",
            immunePoison = false,
            immuneVenom = false,
            examine = "A denizen of the Abyss!"
        )
        val chicken = MonsterDefinition(
            id = 1,
            name = "Chicken",
            members = false,
            combatLevel = 1,
            hitpoints = 3,
            maxHit = "1",
            attackSpeed = null,
            size = 1,
            attackLevel = null,
            strengthLevel = null,
            defenceLevel = null,
            magicLevel = null,
            rangedLevel = null,
            attackStab = null,
            attackSlash = null,
            attackCrush = null,
            attackMagic = null,
            attackRanged = null,
            defenceStab = null,
            defenceSlash = null,
            defenceCrush = null,
            defenceMagic = null,
            defenceRanged = null,
            strengthBonus = null,
            rangedStrengthBonus = null,
            magicDamageBonus = null,
            slayerLevel = null,
            slayerXp = null,
            slayerCategory = null,
            assignedBy = null,
            elementalWeakness = null,
            elementalWeaknessPercent = null,
            poisonous = null,
            immunePoison = null,
            immuneVenom = null,
            examine = null
        )

        val monstersMap = mapOf(
            "415" to abyssalDemon,
            "1" to chicken
        )
        val json = gson.toJson(monstersMap)
        Files.writeString(tempDir.resolve("monsters.json"), json)

        // Write metadata with matching hash so OsrsDataManager serves from disk
        val hash = OsrsDataManager.sha256(json)
        val metadata = Metadata(
            version = 1,
            scrapedAt = "2025-01-15T12:00:00Z",
            files = mapOf("monsters.json" to FileInfo(hash = hash, entries = 2))
        )
        Files.writeString(tempDir.resolve("metadata.json"), gson.toJson(metadata))

        manager = OsrsDataManager(
            cacheDir = tempDir,
            baseUrl = "http://localhost:1/nonexistent"
        )
        db = MonsterDatabase(manager)
    }

    @AfterTest
    fun cleanup() {
        if (::tempDir.isInitialized) {
            tempDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun `get returns monster by id`() {
        val monster = db.get(415)
        assertNotNull(monster)
        assertEquals(415, monster.id)
        assertEquals("Abyssal demon", monster.name)
        assertTrue(monster.members)
        assertEquals(124, monster.combatLevel)
    }

    @Test
    fun `getByName returns monster case insensitively`() {
        val monster1 = db.getByName("Abyssal demon")
        assertNotNull(monster1)
        assertEquals(415, monster1.id)

        val monster2 = db.getByName("ABYSSAL DEMON")
        assertNotNull(monster2)
        assertEquals(415, monster2.id)

        val monster3 = db.getByName("chicken")
        assertNotNull(monster3)
        assertEquals(1, monster3.id)
    }

    @Test
    fun `get returns null for unknown id`() {
        val monster = db.get(99999)
        assertNull(monster)
    }

    @Test
    fun `getAll returns all monsters`() {
        val all = db.getAll()
        assertEquals(2, all.size)
        val ids = all.map { it.id }.toSet()
        assertTrue(ids.contains(415))
        assertTrue(ids.contains(1))
    }
}
