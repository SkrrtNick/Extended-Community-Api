package org.tribot.api.query

import io.mockk.every
import org.tribot.automation.script.core.tabs.EquipmentSlot
import org.tribot.automation.script.core.tabs.EquippedItem
import org.tribot.api.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class EquipmentQueryBuilderTest {

    private fun buildContext(
        items: List<EquippedItem> = emptyList(),
        itemDefs: Map<Int, org.tribot.automation.script.core.definition.ItemDefinition> = emptyMap()
    ): org.tribot.automation.script.ScriptContext {
        return fakeContext {
            every { equipment.getItems() } returns items
            for ((id, def) in itemDefs) {
                every { definitions.getItem(id) } returns def
            }
        }
    }

    @Test
    fun `no filters returns all equipped items`() {
        val items = listOf(
            EquippedItem(4151, 1, EquipmentSlot.WEAPON),
            EquippedItem(1127, 1, EquipmentSlot.BODY)
        )
        val ctx = buildContext(items = items)

        val results = EquipmentQueryBuilder(ctx).results()
        assertEquals(2, results.size)
    }

    @Test
    fun `names filter matches by name`() {
        val items = listOf(
            EquippedItem(4151, 1, EquipmentSlot.WEAPON),
            EquippedItem(1127, 1, EquipmentSlot.BODY)
        )
        val ctx = buildContext(items = items, itemDefs = mapOf(
            4151 to fakeItemDef(id = 4151, name = "Abyssal whip"),
            1127 to fakeItemDef(id = 1127, name = "Rune platebody")
        ))

        val results = EquipmentQueryBuilder(ctx).names("Abyssal whip").results()
        assertEquals(1, results.size)
        assertEquals(4151, results.first()?.id)
    }

    @Test
    fun `ids filter matches by id`() {
        val items = listOf(
            EquippedItem(4151, 1, EquipmentSlot.WEAPON),
            EquippedItem(1127, 1, EquipmentSlot.BODY)
        )
        val ctx = buildContext(items = items)

        val results = EquipmentQueryBuilder(ctx).ids(1127).results()
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
        val ctx = buildContext(items = items)

        val results = EquipmentQueryBuilder(ctx).slots(EquipmentSlot.WEAPON, EquipmentSlot.BODY).results()
        assertEquals(2, results.size)
    }

    @Test
    fun `actions filter matches by inventory action`() {
        val items = listOf(
            EquippedItem(4151, 1, EquipmentSlot.WEAPON),
            EquippedItem(2550, 1, EquipmentSlot.RING)
        )
        val ctx = buildContext(items = items, itemDefs = mapOf(
            4151 to fakeItemDef(id = 4151, name = "Abyssal whip", inventoryActions = listOf("Wield", null, null, "Drop", null)),
            2550 to fakeItemDef(id = 2550, name = "Ring of dueling(8)", inventoryActions = listOf("Wear", "Rub", null, "Drop", null))
        ))

        val results = EquipmentQueryBuilder(ctx).actions("Rub").results()
        assertEquals(1, results.size)
        assertEquals(2550, results.first()?.id)
    }

    @Test
    fun `minQuantity filters by minimum quantity`() {
        val items = listOf(
            EquippedItem(892, 500, EquipmentSlot.ARROW),
            EquippedItem(4151, 1, EquipmentSlot.WEAPON)
        )
        val ctx = buildContext(items = items)

        val results = EquipmentQueryBuilder(ctx).minQuantity(100).results()
        assertEquals(1, results.size)
        assertEquals(892, results.first()?.id)
    }

    @Test
    fun `maxQuantity filters by maximum quantity`() {
        val items = listOf(
            EquippedItem(892, 500, EquipmentSlot.ARROW),
            EquippedItem(4151, 1, EquipmentSlot.WEAPON)
        )
        val ctx = buildContext(items = items)

        val results = EquipmentQueryBuilder(ctx).maxQuantity(10).results()
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
        val ctx = buildContext(items = items, itemDefs = mapOf(
            4151 to fakeItemDef(id = 4151, name = "Abyssal whip"),
            1127 to fakeItemDef(id = 1127, name = "Rune platebody"),
            892 to fakeItemDef(id = 892, name = "Rune arrow")
        ))

        val results = EquipmentQueryBuilder(ctx)
            .slots(EquipmentSlot.WEAPON, EquipmentSlot.ARROW)
            .minQuantity(100)
            .results()
        assertEquals(1, results.size)
        assertEquals(892, results.first()?.id)
    }
}
