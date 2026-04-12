package org.tribot.api.query

import io.mockk.every
import io.mockk.mockk
import net.runelite.api.TileObject
import net.runelite.api.coords.WorldPoint
import org.tribot.api.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ObjectQueryBuilderTest {

    private val playerLocation = WorldPoint(3200, 3200, 0)

    private fun fakeTileObject(id: Int, worldLocation: WorldPoint = WorldPoint(3200, 3200, 0)): TileObject {
        val obj = mockk<TileObject>(relaxed = true)
        every { obj.id } returns id
        every { obj.worldLocation } returns worldLocation
        return obj
    }

    private fun buildContext(
        objects: List<TileObject> = emptyList(),
        objectDefs: Map<Int, org.tribot.automation.script.core.definition.ObjectDefinition> = emptyMap()
    ): org.tribot.automation.script.ScriptContext {
        val localPlayer = fakePlayer(worldLocation = playerLocation)
        return fakeContext {
            every { worldViews.getTopLevelObjects() } returns objects
            every { worldViews.getLocalPlayer() } returns localPlayer
            for ((id, def) in objectDefs) {
                every { definitions.getObject(id) } returns def
            }
        }
    }

    @Test
    fun `names filter matches by name`() {
        val objects = listOf(
            fakeTileObject(id = 1),
            fakeTileObject(id = 2)
        )
        val ctx = buildContext(objects = objects, objectDefs = mapOf(
            1 to fakeObjectDef(id = 1, name = "Oak tree"),
            2 to fakeObjectDef(id = 2, name = "Willow tree")
        ))

        val results = ObjectQueryBuilder(ctx).names("Oak tree").results()
        assertEquals(1, results.size)
    }

    @Test
    fun `actions filter matches by action`() {
        val objects = listOf(
            fakeTileObject(id = 1),
            fakeTileObject(id = 2)
        )
        val ctx = buildContext(objects = objects, objectDefs = mapOf(
            1 to fakeObjectDef(id = 1, name = "Oak tree", actions = listOf("Chop down", null, null, null, null)),
            2 to fakeObjectDef(id = 2, name = "Bank booth", actions = listOf("Use", "Bank", null, null, null))
        ))

        val results = ObjectQueryBuilder(ctx).actions("Bank").results()
        assertEquals(1, results.size)
    }

    @Test
    fun `withinDistance filters by distance`() {
        val objects = listOf(
            fakeTileObject(id = 1, worldLocation = WorldPoint(3202, 3200, 0)),  // distance 2
            fakeTileObject(id = 2, worldLocation = WorldPoint(3220, 3200, 0))   // distance 20
        )
        val ctx = buildContext(objects = objects, objectDefs = mapOf(
            1 to fakeObjectDef(id = 1, name = "Close"),
            2 to fakeObjectDef(id = 2, name = "Far")
        ))

        val results = ObjectQueryBuilder(ctx).withinDistance(10).results()
        assertEquals(1, results.size)
    }

    @Test
    fun `ids filter matches by id`() {
        val objects = listOf(
            fakeTileObject(id = 100),
            fakeTileObject(id = 200)
        )
        val ctx = buildContext(objects = objects, objectDefs = mapOf(
            100 to fakeObjectDef(id = 100, name = "Tree"),
            200 to fakeObjectDef(id = 200, name = "Rock")
        ))

        val results = ObjectQueryBuilder(ctx).ids(200).results()
        assertEquals(1, results.size)
    }
}
