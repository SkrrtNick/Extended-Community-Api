package org.tribot.api.query

import io.mockk.every
import org.tribot.automation.script.core.tabs.InventoryItem
import org.tribot.api.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class InventoryQueryBuilderTest {

    private fun buildContext(
        items: List<InventoryItem> = emptyList(),
        itemDefs: Map<Int, org.tribot.automation.script.core.definition.ItemDefinition> = emptyMap()
    ): org.tribot.automation.script.ScriptContext {
        return fakeContext {
            every { inventory.getItems() } returns items
            for ((id, def) in itemDefs) {
                every { definitions.getItem(id) } returns def
            }
        }
    }

    @Test
    fun `names filter matches by name`() {
        val items = listOf(
            InventoryItem(995, 100, 0),
            InventoryItem(526, 1, 1)
        )
        val ctx = buildContext(items = items, itemDefs = mapOf(
            995 to fakeItemDef(id = 995, name = "Coins"),
            526 to fakeItemDef(id = 526, name = "Bones")
        ))

        val results = InventoryQueryBuilder(ctx).names("Coins").results()
        assertEquals(1, results.size)
        assertEquals(995, results.first()?.id)
    }

    @Test
    fun `ids filter matches by id`() {
        val items = listOf(
            InventoryItem(995, 100, 0),
            InventoryItem(526, 1, 1)
        )
        val ctx = buildContext(items = items, itemDefs = mapOf(
            995 to fakeItemDef(id = 995, name = "Coins"),
            526 to fakeItemDef(id = 526, name = "Bones")
        ))

        val results = InventoryQueryBuilder(ctx).ids(526).results()
        assertEquals(1, results.size)
        assertEquals(526, results.first()?.id)
    }

    @Test
    fun `actions filter matches by inventory action`() {
        val items = listOf(
            InventoryItem(995, 100, 0),
            InventoryItem(526, 1, 1)
        )
        val ctx = buildContext(items = items, itemDefs = mapOf(
            995 to fakeItemDef(id = 995, name = "Coins", inventoryActions = listOf("Use", null, null, "Drop", null)),
            526 to fakeItemDef(id = 526, name = "Bones", inventoryActions = listOf("Bury", null, null, "Drop", null))
        ))

        val results = InventoryQueryBuilder(ctx).actions("Bury").results()
        assertEquals(1, results.size)
        assertEquals(526, results.first()?.id)
    }

    @Test
    fun `minQuantity filters by minimum quantity`() {
        val items = listOf(
            InventoryItem(995, 100, 0),
            InventoryItem(526, 1, 1)
        )
        val ctx = buildContext(items = items, itemDefs = mapOf(
            995 to fakeItemDef(id = 995, name = "Coins"),
            526 to fakeItemDef(id = 526, name = "Bones")
        ))

        val results = InventoryQueryBuilder(ctx).minQuantity(50).results()
        assertEquals(1, results.size)
        assertEquals(995, results.first()?.id)
    }

    @Test
    fun `maxQuantity filters by maximum quantity`() {
        val items = listOf(
            InventoryItem(995, 100, 0),
            InventoryItem(526, 1, 1)
        )
        val ctx = buildContext(items = items, itemDefs = mapOf(
            995 to fakeItemDef(id = 995, name = "Coins"),
            526 to fakeItemDef(id = 526, name = "Bones")
        ))

        val results = InventoryQueryBuilder(ctx).maxQuantity(10).results()
        assertEquals(1, results.size)
        assertEquals(526, results.first()?.id)
    }
}
