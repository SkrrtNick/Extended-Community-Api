package org.tribot.api.query

import io.mockk.every
import io.mockk.mockk
import net.runelite.api.TileItem
import net.runelite.api.coords.WorldPoint
import org.tribot.automation.script.core.GroundItem
import org.tribot.api.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class GroundItemQueryBuilderTest {

    private val playerLocation = WorldPoint(3200, 3200, 0)

    private fun fakeGroundItem(
        id: Int,
        quantity: Int = 1,
        position: WorldPoint = WorldPoint(3200, 3200, 0)
    ): GroundItem {
        val tileItem = mockk<TileItem>(relaxed = true)
        every { tileItem.id } returns id
        every { tileItem.quantity } returns quantity
        return GroundItem(tileItem, position)
    }

    private fun buildContext(
        groundItems: List<GroundItem> = emptyList(),
        itemDefs: Map<Int, org.tribot.automation.script.core.definition.ItemDefinition> = emptyMap()
    ): org.tribot.automation.script.ScriptContext {
        val localPlayer = fakePlayer(worldLocation = playerLocation)
        return fakeContext {
            every { worldViews.getTopLevelGroundItems() } returns groundItems
            every { worldViews.getLocalPlayer() } returns localPlayer
            for ((id, def) in itemDefs) {
                every { definitions.getItem(id) } returns def
            }
        }
    }

    @Test
    fun `names filter matches by name`() {
        val items = listOf(
            fakeGroundItem(id = 995, quantity = 100),
            fakeGroundItem(id = 526, quantity = 1)
        )
        val ctx = buildContext(groundItems = items, itemDefs = mapOf(
            995 to fakeItemDef(id = 995, name = "Coins"),
            526 to fakeItemDef(id = 526, name = "Bones")
        ))

        val results = GroundItemQueryBuilder(ctx).names("Coins").results()
        assertEquals(1, results.size)
        assertEquals(995, results.first()?.id)
    }

    @Test
    fun `ids filter matches by id`() {
        val items = listOf(
            fakeGroundItem(id = 995),
            fakeGroundItem(id = 526)
        )
        val ctx = buildContext(groundItems = items, itemDefs = mapOf(
            995 to fakeItemDef(id = 995, name = "Coins"),
            526 to fakeItemDef(id = 526, name = "Bones")
        ))

        val results = GroundItemQueryBuilder(ctx).ids(526).results()
        assertEquals(1, results.size)
        assertEquals(526, results.first()?.id)
    }

    @Test
    fun `minQuantity filters by minimum stack size`() {
        val items = listOf(
            fakeGroundItem(id = 995, quantity = 100),
            fakeGroundItem(id = 996, quantity = 5)
        )
        val ctx = buildContext(groundItems = items, itemDefs = mapOf(
            995 to fakeItemDef(id = 995, name = "Coins"),
            996 to fakeItemDef(id = 996, name = "Coins2")
        ))

        val results = GroundItemQueryBuilder(ctx).minQuantity(50).results()
        assertEquals(1, results.size)
        assertEquals(100, results.first()?.quantity)
    }

    @Test
    fun `maxQuantity filters by maximum stack size`() {
        val items = listOf(
            fakeGroundItem(id = 995, quantity = 100),
            fakeGroundItem(id = 996, quantity = 5)
        )
        val ctx = buildContext(groundItems = items, itemDefs = mapOf(
            995 to fakeItemDef(id = 995, name = "Coins"),
            996 to fakeItemDef(id = 996, name = "Coins2")
        ))

        val results = GroundItemQueryBuilder(ctx).maxQuantity(10).results()
        assertEquals(1, results.size)
        assertEquals(5, results.first()?.quantity)
    }

    @Test
    fun `withinDistance filters by distance`() {
        val items = listOf(
            fakeGroundItem(id = 995, position = WorldPoint(3201, 3200, 0)),  // distance 1
            fakeGroundItem(id = 526, position = WorldPoint(3220, 3200, 0))   // distance 20
        )
        val ctx = buildContext(groundItems = items, itemDefs = mapOf(
            995 to fakeItemDef(id = 995, name = "Coins"),
            526 to fakeItemDef(id = 526, name = "Bones")
        ))

        val results = GroundItemQueryBuilder(ctx).withinDistance(10).results()
        assertEquals(1, results.size)
        assertEquals(995, results.first()?.id)
    }
}
