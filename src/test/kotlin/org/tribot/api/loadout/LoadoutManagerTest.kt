package org.tribot.api.loadout

import io.mockk.every
import org.tribot.api.ApiContext
import org.tribot.api.testing.fakeContext
import org.tribot.automation.script.core.tabs.EquipmentSlot
import org.tribot.automation.script.core.tabs.EquippedItem
import org.tribot.automation.script.core.tabs.InventoryItem
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoadoutManagerTest {

    @AfterTest
    fun tearDown() {
        ApiContext.reset()
    }

    @Test
    fun `isSatisfied returns true when loadout matches`() {
        ApiContext.init(fakeContext {
            every { inventory.getItems() } returns listOf(
                InventoryItem(555, 100, 0), // Water rune
                InventoryItem(554, 200, 1)  // Fire rune
            )
            every { equipment.getItems() } returns listOf(
                EquippedItem(4151, 1, EquipmentSlot.WEAPON), // Abyssal whip
                EquippedItem(1540, 1, EquipmentSlot.SHIELD)  // Dragon defender
            )
        })
        val loadout = Loadout(
            inventory = listOf(
                LoadoutItem(555, 50),
                LoadoutItem(554, 100)
            ),
            equipment = listOf(
                LoadoutItem(4151, 1, EquipmentSlot.WEAPON),
                LoadoutItem(1540, 1, EquipmentSlot.SHIELD)
            )
        )
        assertTrue(LoadoutManager.isSatisfied(loadout))
    }

    @Test
    fun `isSatisfied returns false when items missing`() {
        ApiContext.init(fakeContext {
            every { inventory.getItems() } returns listOf(
                InventoryItem(555, 100, 0) // only water rune
            )
            every { equipment.getItems() } returns listOf(
                EquippedItem(4151, 1, EquipmentSlot.WEAPON)
            )
        })
        val loadout = Loadout(
            inventory = listOf(
                LoadoutItem(555, 50),
                LoadoutItem(554, 100) // Fire rune missing from inventory
            ),
            equipment = listOf(
                LoadoutItem(4151, 1, EquipmentSlot.WEAPON)
            )
        )
        assertFalse(LoadoutManager.isSatisfied(loadout))
    }

    @Test
    fun `isSatisfied returns false when equipment missing`() {
        ApiContext.init(fakeContext {
            every { inventory.getItems() } returns listOf(
                InventoryItem(555, 100, 0)
            )
            every { equipment.getItems() } returns emptyList()
        })
        val loadout = Loadout(
            inventory = listOf(LoadoutItem(555, 50)),
            equipment = listOf(LoadoutItem(4151, 1, EquipmentSlot.WEAPON))
        )
        assertFalse(LoadoutManager.isSatisfied(loadout))
    }

    @Test
    fun `getMissingItems returns items not in inventory`() {
        ApiContext.init(fakeContext {
            every { inventory.getItems() } returns listOf(
                InventoryItem(555, 30, 0) // Only 30 water runes
            )
        })
        val loadout = Loadout(
            inventory = listOf(
                LoadoutItem(555, 100),  // Need 100 water runes
                LoadoutItem(554, 200)   // Need 200 fire runes (have 0)
            )
        )
        val missing = LoadoutManager.getMissingItems(loadout)
        assertEquals(2, missing.size)

        val waterRune = missing.find { it.itemId == 555 }!!
        assertEquals(70, waterRune.quantity) // deficit: 100 - 30

        val fireRune = missing.find { it.itemId == 554 }!!
        assertEquals(200, fireRune.quantity) // deficit: 200 - 0
    }

    @Test
    fun `getMissingItems returns empty when all items present`() {
        ApiContext.init(fakeContext {
            every { inventory.getItems() } returns listOf(
                InventoryItem(555, 100, 0),
                InventoryItem(554, 200, 1)
            )
        })
        val loadout = Loadout(
            inventory = listOf(
                LoadoutItem(555, 100),
                LoadoutItem(554, 200)
            )
        )
        val missing = LoadoutManager.getMissingItems(loadout)
        assertTrue(missing.isEmpty())
    }

    @Test
    fun `getUnwantedItems returns inventory items not in loadout`() {
        ApiContext.init(fakeContext {
            every { inventory.getItems() } returns listOf(
                InventoryItem(555, 100, 0), // Water rune (wanted)
                InventoryItem(995, 5000, 1), // Coins (not wanted)
                InventoryItem(1234, 1, 2)    // Some junk (not wanted)
            )
        })
        val loadout = Loadout(
            inventory = listOf(LoadoutItem(555, 100))
        )
        val unwanted = LoadoutManager.getUnwantedItems(loadout)
        assertEquals(2, unwanted.size)
        assertTrue(unwanted.any { it.id == 995 })
        assertTrue(unwanted.any { it.id == 1234 })
    }

    @Test
    fun `getUnwantedItems returns empty when all items are wanted`() {
        ApiContext.init(fakeContext {
            every { inventory.getItems() } returns listOf(
                InventoryItem(555, 100, 0)
            )
        })
        val loadout = Loadout(
            inventory = listOf(LoadoutItem(555, 100))
        )
        val unwanted = LoadoutManager.getUnwantedItems(loadout)
        assertTrue(unwanted.isEmpty())
    }

    @Test
    fun `getMissingEquipment returns equipment not worn`() {
        ApiContext.init(fakeContext {
            every { equipment.getItems() } returns listOf(
                EquippedItem(4151, 1, EquipmentSlot.WEAPON) // Only whip equipped
            )
        })
        val loadout = Loadout(
            equipment = listOf(
                LoadoutItem(4151, 1, EquipmentSlot.WEAPON), // Whip (equipped)
                LoadoutItem(1540, 1, EquipmentSlot.SHIELD)  // Defender (missing)
            )
        )
        val missing = LoadoutManager.getMissingEquipment(loadout)
        assertEquals(1, missing.size)
        assertEquals(1540, missing[0].itemId)
    }

    @Test
    fun `getMissingEquipment returns empty when all equipped`() {
        ApiContext.init(fakeContext {
            every { equipment.getItems() } returns listOf(
                EquippedItem(4151, 1, EquipmentSlot.WEAPON),
                EquippedItem(1540, 1, EquipmentSlot.SHIELD)
            )
        })
        val loadout = Loadout(
            equipment = listOf(
                LoadoutItem(4151, 1, EquipmentSlot.WEAPON),
                LoadoutItem(1540, 1, EquipmentSlot.SHIELD)
            )
        )
        val missing = LoadoutManager.getMissingEquipment(loadout)
        assertTrue(missing.isEmpty())
    }

    @Test
    fun `alternateIds are considered when checking satisfaction`() {
        ApiContext.init(fakeContext {
            every { inventory.getItems() } returns listOf(
                InventoryItem(996, 100, 0) // Alternate ID for coins
            )
            every { equipment.getItems() } returns listOf(
                EquippedItem(4152, 1, EquipmentSlot.WEAPON) // Alternate ID for whip
            )
        })
        val loadout = Loadout(
            inventory = listOf(
                LoadoutItem(995, 100, alternateIds = listOf(996, 997))
            ),
            equipment = listOf(
                LoadoutItem(4151, 1, EquipmentSlot.WEAPON, alternateIds = listOf(4152))
            )
        )
        assertTrue(LoadoutManager.isSatisfied(loadout))
    }

    @Test
    fun `alternateIds are considered for getMissingItems`() {
        ApiContext.init(fakeContext {
            every { inventory.getItems() } returns listOf(
                InventoryItem(996, 50, 0) // Alternate ID for coins, only 50
            )
        })
        val loadout = Loadout(
            inventory = listOf(
                LoadoutItem(995, 100, alternateIds = listOf(996))
            )
        )
        val missing = LoadoutManager.getMissingItems(loadout)
        assertEquals(1, missing.size)
        assertEquals(995, missing[0].itemId)
        assertEquals(50, missing[0].quantity) // deficit: 100 - 50
    }

    @Test
    fun `alternateIds are considered for getMissingEquipment`() {
        ApiContext.init(fakeContext {
            every { equipment.getItems() } returns listOf(
                EquippedItem(4152, 1, EquipmentSlot.WEAPON) // Alternate ID
            )
        })
        val loadout = Loadout(
            equipment = listOf(
                LoadoutItem(4151, 1, EquipmentSlot.WEAPON, alternateIds = listOf(4152))
            )
        )
        val missing = LoadoutManager.getMissingEquipment(loadout)
        assertTrue(missing.isEmpty())
    }

    @Test
    fun `getUnwantedItems considers alternate IDs as wanted`() {
        ApiContext.init(fakeContext {
            every { inventory.getItems() } returns listOf(
                InventoryItem(996, 100, 0) // Alternate ID for coins
            )
        })
        val loadout = Loadout(
            inventory = listOf(
                LoadoutItem(995, 100, alternateIds = listOf(996))
            )
        )
        val unwanted = LoadoutManager.getUnwantedItems(loadout)
        assertTrue(unwanted.isEmpty())
    }
}
