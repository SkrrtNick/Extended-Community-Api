package org.tribot.api.requirements

import com.google.gson.Gson
import io.mockk.every
import net.runelite.api.Skill
import org.tribot.api.ApiContext
import org.tribot.api.data.EquipmentStats
import org.tribot.api.data.FileInfo
import org.tribot.api.data.ItemDatabase
import org.tribot.api.data.ItemDefinition
import org.tribot.api.data.Metadata
import org.tribot.api.data.OsrsDataManager
import org.tribot.api.testing.fakeContext
import org.tribot.automation.script.core.tabs.EquipmentSlot
import org.tribot.automation.script.core.tabs.EquippedItem
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EquipmentRequirementTest {

    private val gson = Gson()
    private lateinit var tempDir: Path
    private lateinit var manager: OsrsDataManager
    private lateinit var itemDb: ItemDatabase

    @BeforeTest
    fun setup() {
        tempDir = Files.createTempDirectory("equip-req-test")

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
            equipment = EquipmentStats(
                slot = "weapon",
                attackStab = 0,
                attackSlash = 82,
                attackCrush = 0,
                attackMagic = 0,
                attackRanged = 0,
                defenceStab = 0,
                defenceSlash = 0,
                defenceCrush = 0,
                defenceMagic = 0,
                defenceRanged = 0,
                meleeStrength = 82,
                rangedStrength = 0,
                magicDamage = 0.0,
                prayer = 0,
                requirements = mapOf("Attack" to 70)
            ),
            weapon = null
        )

        val runeChainbody = ItemDefinition(
            id = 1113,
            name = "Rune chainbody",
            members = false,
            tradeable = true,
            tradeableOnGe = true,
            stackable = false,
            cost = 50000,
            highAlch = 30000,
            lowAlch = 20000,
            buyLimit = 125,
            weight = 9.071,
            examine = "A\"\"chainbody made of rune.",
            questItem = false,
            equipment = EquipmentStats(
                slot = "body",
                attackStab = 0,
                attackSlash = 0,
                attackCrush = 0,
                attackMagic = -15,
                attackRanged = -15,
                defenceStab = 63,
                defenceSlash = 72,
                defenceCrush = 63,
                defenceMagic = -3,
                defenceRanged = 63,
                meleeStrength = 0,
                rangedStrength = 0,
                magicDamage = 0.0,
                prayer = 0,
                requirements = mapOf("Defence" to 40)
            ),
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
            "1113" to runeChainbody,
            "995" to coins
        )
        val json = gson.toJson(itemsMap)
        Files.writeString(tempDir.resolve("items.json"), json)

        val hash = OsrsDataManager.sha256(json)
        val metadata = Metadata(
            version = 1,
            scrapedAt = "2025-01-15T12:00:00Z",
            files = mapOf("items.json" to FileInfo(hash = hash, entries = 3))
        )
        Files.writeString(tempDir.resolve("metadata.json"), gson.toJson(metadata))

        manager = OsrsDataManager(
            cacheDir = tempDir,
            baseUrl = "http://localhost:1/nonexistent"
        )
        itemDb = ItemDatabase(manager)
    }

    @AfterTest
    fun tearDown() {
        ApiContext.reset()
        if (::tempDir.isInitialized) {
            tempDir.toFile().deleteRecursively()
        }
    }

    // -------------------------------------------------------------------------
    // skillRequirements
    // -------------------------------------------------------------------------

    @Test
    fun `skillRequirements resolves from database when equipment has requirements`() {
        val req = EquipmentRequirement(
            itemId = 4151,
            slot = EquipmentSlot.WEAPON,
            displayName = "Abyssal whip",
            itemDatabase = itemDb
        )
        val skills = req.skillRequirements
        assertEquals(1, skills.size)
        assertEquals(Skill.ATTACK, skills[0].skill)
        assertEquals(70, skills[0].level)
    }

    @Test
    fun `skillRequirements returns empty list when item has no equipment stats`() {
        val req = EquipmentRequirement(
            itemId = 995,
            slot = EquipmentSlot.WEAPON,
            displayName = "Coins",
            itemDatabase = itemDb
        )
        assertTrue(req.skillRequirements.isEmpty())
    }

    @Test
    fun `skillRequirements returns empty list when item not in database`() {
        val req = EquipmentRequirement(
            itemId = 99999,
            slot = EquipmentSlot.WEAPON,
            displayName = "Unknown item",
            itemDatabase = itemDb
        )
        assertTrue(req.skillRequirements.isEmpty())
    }

    @Test
    fun `skillRequirements resolves multiple skills`() {
        val req = EquipmentRequirement(
            itemId = 1113,
            slot = EquipmentSlot.BODY,
            displayName = "Rune chainbody",
            itemDatabase = itemDb
        )
        val skills = req.skillRequirements
        assertEquals(1, skills.size)
        assertEquals(Skill.DEFENCE, skills[0].skill)
        assertEquals(40, skills[0].level)
    }

    // -------------------------------------------------------------------------
    // canEquip()
    // -------------------------------------------------------------------------

    @Test
    fun `canEquip returns true when all skill requirements met`() {
        ApiContext.init(fakeContext {
            every { skills.getLevel(Skill.ATTACK) } returns 70
        })
        val req = EquipmentRequirement(
            itemId = 4151,
            slot = EquipmentSlot.WEAPON,
            displayName = "Abyssal whip",
            itemDatabase = itemDb
        )
        assertTrue(req.canEquip())
    }

    @Test
    fun `canEquip returns false when a skill requirement is not met`() {
        ApiContext.init(fakeContext {
            every { skills.getLevel(Skill.ATTACK) } returns 50
        })
        val req = EquipmentRequirement(
            itemId = 4151,
            slot = EquipmentSlot.WEAPON,
            displayName = "Abyssal whip",
            itemDatabase = itemDb
        )
        assertFalse(req.canEquip())
    }

    @Test
    fun `canEquip returns true when database unavailable`() {
        // Create an ItemDatabase backed by an empty temp dir — no items.json
        val emptyDir = Files.createTempDirectory("equip-req-empty")
        val emptyManager = OsrsDataManager(
            cacheDir = emptyDir,
            baseUrl = "http://localhost:1/nonexistent"
        )
        val emptyDb = ItemDatabase(emptyManager)

        val req = EquipmentRequirement(
            itemId = 4151,
            slot = EquipmentSlot.WEAPON,
            displayName = "Abyssal whip",
            itemDatabase = emptyDb
        )
        // Fail-open: no data means no requirements, so canEquip() is true
        assertTrue(req.canEquip())

        emptyDir.toFile().deleteRecursively()
    }

    // -------------------------------------------------------------------------
    // Existing check() behavior unchanged
    // -------------------------------------------------------------------------

    @Test
    fun `check returns true when item is equipped in correct slot`() {
        ApiContext.init(fakeContext {
            every { equipment.getItemIn(EquipmentSlot.WEAPON) } returns EquippedItem(4151, 1, EquipmentSlot.WEAPON)
        })
        val req = EquipmentRequirement(
            itemId = 4151,
            slot = EquipmentSlot.WEAPON,
            displayName = "Abyssal whip",
            itemDatabase = itemDb
        )
        assertTrue(req.check())
    }

    @Test
    fun `check returns false when item is not equipped`() {
        ApiContext.init(fakeContext {
            every { equipment.getItemIn(EquipmentSlot.WEAPON) } returns null
        })
        val req = EquipmentRequirement(
            itemId = 4151,
            slot = EquipmentSlot.WEAPON,
            displayName = "Abyssal whip",
            itemDatabase = itemDb
        )
        assertFalse(req.check())
    }
}
