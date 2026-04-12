package org.tribot.api.query

import io.mockk.every
import org.tribot.automation.script.core.widgets.BankItem
import org.tribot.api.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class BankQueryBuilderTest {

    private fun buildContext(
        items: List<BankItem> = emptyList(),
        itemDefs: Map<Int, org.tribot.automation.script.core.definition.ItemDefinition> = emptyMap()
    ): org.tribot.automation.script.ScriptContext {
        return fakeContext {
            every { banking.getItems() } returns items
            for ((id, def) in itemDefs) {
                every { definitions.getItem(id) } returns def
            }
        }
    }

    @Test
    fun `names filter matches by name`() {
        val items = listOf(
            BankItem(995, 10000),
            BankItem(526, 50)
        )
        val ctx = buildContext(items = items, itemDefs = mapOf(
            995 to fakeItemDef(id = 995, name = "Coins"),
            526 to fakeItemDef(id = 526, name = "Bones")
        ))

        val results = BankQueryBuilder(ctx).names("Coins").results()
        assertEquals(1, results.size)
        assertEquals(995, results.first()?.id)
    }

    @Test
    fun `minQuantity filters by minimum quantity`() {
        val items = listOf(
            BankItem(995, 10000),
            BankItem(526, 50)
        )
        val ctx = buildContext(items = items, itemDefs = mapOf(
            995 to fakeItemDef(id = 995, name = "Coins"),
            526 to fakeItemDef(id = 526, name = "Bones")
        ))

        val results = BankQueryBuilder(ctx).minQuantity(100).results()
        assertEquals(1, results.size)
        assertEquals(995, results.first()?.id)
    }

    @Test
    fun `ids filter matches by id`() {
        val items = listOf(
            BankItem(995, 10000),
            BankItem(526, 50)
        )
        val ctx = buildContext(items = items, itemDefs = mapOf(
            995 to fakeItemDef(id = 995, name = "Coins"),
            526 to fakeItemDef(id = 526, name = "Bones")
        ))

        val results = BankQueryBuilder(ctx).ids(526).results()
        assertEquals(1, results.size)
        assertEquals(526, results.first()?.id)
    }

    @Test
    fun `actions filter matches by inventory action`() {
        val items = listOf(
            BankItem(995, 10000),
            BankItem(526, 50)
        )
        val ctx = buildContext(items = items, itemDefs = mapOf(
            995 to fakeItemDef(id = 995, name = "Coins", inventoryActions = listOf("Use", null, null, "Drop", null)),
            526 to fakeItemDef(id = 526, name = "Bones", inventoryActions = listOf("Bury", null, null, "Drop", null))
        ))

        val results = BankQueryBuilder(ctx).actions("Bury").results()
        assertEquals(1, results.size)
        assertEquals(526, results.first()?.id)
    }

    @Test
    fun `maxQuantity filters by maximum quantity`() {
        val items = listOf(
            BankItem(995, 10000),
            BankItem(526, 50)
        )
        val ctx = buildContext(items = items, itemDefs = mapOf(
            995 to fakeItemDef(id = 995, name = "Coins"),
            526 to fakeItemDef(id = 526, name = "Bones")
        ))

        val results = BankQueryBuilder(ctx).maxQuantity(100).results()
        assertEquals(1, results.size)
        assertEquals(526, results.first()?.id)
    }
}
