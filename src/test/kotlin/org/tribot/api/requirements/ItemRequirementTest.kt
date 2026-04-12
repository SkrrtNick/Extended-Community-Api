package org.tribot.api.requirements

import io.mockk.every
import org.tribot.api.testing.fakeContext
import org.tribot.automation.script.core.tabs.EquipmentSlot
import org.tribot.automation.script.core.tabs.EquippedItem
import org.tribot.automation.script.core.tabs.InventoryItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ItemRequirementTest {

    @Test
    fun `satisfied with enough quantity`() {
        val ctx = fakeContext {
            every { inventory.getItems() } returns listOf(
                InventoryItem(995, 100, 0)
            )
        }
        val req = ItemRequirement(itemId = 995, quantity = 50, displayName = "Coins")
        assertTrue(req.check(ctx))
    }

    @Test
    fun `satisfied with exact quantity`() {
        val ctx = fakeContext {
            every { inventory.getItems() } returns listOf(
                InventoryItem(995, 50, 0)
            )
        }
        val req = ItemRequirement(itemId = 995, quantity = 50, displayName = "Coins")
        assertTrue(req.check(ctx))
    }

    @Test
    fun `not satisfied with insufficient quantity`() {
        val ctx = fakeContext {
            every { inventory.getItems() } returns listOf(
                InventoryItem(995, 10, 0)
            )
        }
        val req = ItemRequirement(itemId = 995, quantity = 50, displayName = "Coins")
        assertFalse(req.check(ctx))
    }

    @Test
    fun `not satisfied when item is missing`() {
        val ctx = fakeContext {
            every { inventory.getItems() } returns emptyList()
        }
        val req = ItemRequirement(itemId = 995, quantity = 1, displayName = "Coins")
        assertFalse(req.check(ctx))
    }

    @Test
    fun `accepts alternate IDs`() {
        val ctx = fakeContext {
            every { inventory.getItems() } returns listOf(
                InventoryItem(996, 5, 0) // alternate ID
            )
        }
        val req = ItemRequirement(
            itemId = 995,
            quantity = 5,
            alternateIds = listOf(996, 997),
            displayName = "Coins"
        )
        assertTrue(req.check(ctx))
    }

    @Test
    fun `equipped flag checks equipment instead of inventory`() {
        val ctx = fakeContext {
            every { inventory.getItems() } returns emptyList()
            every { equipment.getItems() } returns listOf(
                EquippedItem(4151, 1, EquipmentSlot.WEAPON)
            )
        }
        val req = ItemRequirement(
            itemId = 4151,
            quantity = 1,
            equipped = true,
            displayName = "Abyssal whip"
        )
        assertTrue(req.check(ctx))
    }

    @Test
    fun `equipped flag fails when not in equipment`() {
        val ctx = fakeContext {
            every { equipment.getItems() } returns emptyList()
        }
        val req = ItemRequirement(
            itemId = 4151,
            quantity = 1,
            equipped = true,
            displayName = "Abyssal whip"
        )
        assertFalse(req.check(ctx))
    }

    @Test
    fun `sums quantity across multiple inventory slots`() {
        val ctx = fakeContext {
            every { inventory.getItems() } returns listOf(
                InventoryItem(554, 200, 0),
                InventoryItem(554, 150, 1)
            )
        }
        val req = ItemRequirement(itemId = 554, quantity = 300, displayName = "Fire rune")
        assertTrue(req.check(ctx))
    }

    @Test
    fun `sums quantity across primary and alternate IDs`() {
        val ctx = fakeContext {
            every { inventory.getItems() } returns listOf(
                InventoryItem(995, 30, 0),
                InventoryItem(996, 25, 1)
            )
        }
        val req = ItemRequirement(
            itemId = 995,
            quantity = 50,
            alternateIds = listOf(996),
            displayName = "Coins"
        )
        assertTrue(req.check(ctx))
    }

    @Test
    fun `displayText without equipped`() {
        val req = ItemRequirement(itemId = 995, quantity = 100, displayName = "Coins")
        assertEquals("100 x Coins", req.displayText)
    }

    @Test
    fun `displayText with equipped`() {
        val req = ItemRequirement(
            itemId = 4151,
            quantity = 1,
            equipped = true,
            displayName = "Abyssal whip"
        )
        assertEquals("1 x Abyssal whip (equipped)", req.displayText)
    }

    @Test
    fun `displayText defaults to item ID`() {
        val req = ItemRequirement(itemId = 4151)
        assertEquals("1 x Item #4151", req.displayText)
    }
}
