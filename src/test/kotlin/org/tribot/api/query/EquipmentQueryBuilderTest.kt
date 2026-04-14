package org.tribot.api.query

import io.mockk.every
import org.tribot.automation.script.core.tabs.EquipmentSlot
import org.tribot.automation.script.core.tabs.EquippedItem
import org.tribot.api.ApiContext
import org.tribot.api.testing.*
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EquipmentQueryBuilderTest {

    @AfterTest
    fun tearDown() {
        ApiContext.reset()
    }

    private fun buildContext(
        items: List<EquippedItem> = emptyList(),
        itemDefs: Map<Int, org.tribot.automation.script.core.definition.ItemDefinition> = emptyMap()
    ) {
        val ctx = fakeContext {
            every { equipment.getItems() } returns items
            for ((id, def) in itemDefs) {
                every { definitions.getItem(id) } returns def
            }
        }
        ApiContext.init(ctx)
    }

    @Test
    fun `no filters returns all equipped items`() {
        val items = listOf(
            EquippedItem(4151, 1, EquipmentSlot.WEAPON),
            EquippedItem(1127, 1, EquipmentSlot.BODY)
        )
        buildContext(items = items)

        val results = EquipmentQueryBuilder().results()
        assertEquals(2, results.size)
    }

    @Test
    fun `names filter matches by name`() {
        val items = listOf(
            EquippedItem(4151, 1, EquipmentSlot.WEAPON),
            EquippedItem(1127, 1, EquipmentSlot.BODY)
        )
        buildContext(items = items, itemDefs = mapOf(
            4151 to fakeItemDef(id = 4151, name = "Abyssal whip"),
            1127 to fakeItemDef(id = 1127, name = "Rune platebody")
        ))

        val results = EquipmentQueryBuilder().names("Abyssal whip").results()
        assertEquals(1, results.size)
        assertEquals(4151, results.first()?.id)
    }

    @Test
    fun `ids filter matches by id`() {
        val items = listOf(
            EquippedItem(4151, 1, EquipmentSlot.WEAPON),
            EquippedItem(1127, 1, EquipmentSlot.BODY)
        )
        buildContext(items = items)

        val results = EquipmentQueryBuilder().ids(1127).results()
        assertEquals(1, results.size)
        assertEquals(1127, results.first()?.id)
    }

    @Test
    fun `slots filter matches by equipment slot`() {
        val items = listOf(
            EquippedItem(4151, 1, EquipmentSlot.WEAPON),
            EquippedItem(1127, 1, EquipmentSlot.BODY),
            EquippedItem(1079, 1, EquipmentSlot.LEGS)
        )
        buildContext(items = items)

        val results = EquipmentQueryBuilder().slots(EquipmentSlot.WEAPON, EquipmentSlot.BODY).results()
        assertEquals(2, results.size)
    }

    @Test
    fun `actions filter matches by inventory action`() {
        val items = listOf(
            EquippedItem(4151, 1, EquipmentSlot.WEAPON),
            EquippedItem(2550, 1, EquipmentSlot.RING)
        )
        buildContext(items = items, itemDefs = mapOf(
            4151 to fakeItemDef(id = 4151, name = "Abyssal whip", inventoryActions = listOf("Wield", null, null, "Drop", null)),
            2550 to fakeItemDef(id = 2550, name = "Ring of dueling(8)", inventoryActions = listOf("Wear", "Rub", null, "Drop", null))
        ))

        val results = EquipmentQueryBuilder().actions("Rub").results()
        assertEquals(1, results.size)
        assertEquals(2550, results.first()?.id)
    }

    @Test
    fun `minQuantity filters by minimum quantity`() {
        val items = listOf(
            EquippedItem(892, 500, EquipmentSlot.ARROW),
            EquippedItem(4151, 1, EquipmentSlot.WEAPON)
        )
        buildContext(items = items)

        val results = EquipmentQueryBuilder().minQuantity(100).results()
        assertEquals(1, results.size)
        assertEquals(892, results.first()?.id)
    }

    @Test
    fun `maxQuantity filters by maximum quantity`() {
        val items = listOf(
            EquippedItem(892, 500, EquipmentSlot.ARROW),
            EquippedItem(4151, 1, EquipmentSlot.WEAPON)
        )
        buildContext(items = items)

        val results = EquipmentQueryBuilder().maxQuantity(10).results()
        assertEquals(1, results.size)
        assertEquals(4151, results.first()?.id)
    }

    @Test
    fun `chaining multiple filters`() {
        val items = listOf(
            EquippedItem(4151, 1, EquipmentSlot.WEAPON),
            EquippedItem(1127, 1, EquipmentSlot.BODY),
            EquippedItem(892, 500, EquipmentSlot.ARROW)
        )
        buildContext(items = items, itemDefs = mapOf(
            4151 to fakeItemDef(id = 4151, name = "Abyssal whip"),
            1127 to fakeItemDef(id = 1127, name = "Rune platebody"),
            892 to fakeItemDef(id = 892, name = "Rune arrow")
        ))

        val results = EquipmentQueryBuilder()
            .slots(EquipmentSlot.WEAPON, EquipmentSlot.ARROW)
            .minQuantity(100)
            .results()
        assertEquals(1, results.size)
        assertEquals(892, results.first()?.id)
    }
}
