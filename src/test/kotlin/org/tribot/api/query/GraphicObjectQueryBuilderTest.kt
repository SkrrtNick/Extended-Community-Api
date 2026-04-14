package org.tribot.api.query

import io.mockk.every
import io.mockk.mockk
import net.runelite.api.GraphicsObject
import org.tribot.api.ApiContext
import org.tribot.api.testing.*
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphicObjectQueryBuilderTest {

    @AfterTest
    fun tearDown() {
        ApiContext.reset()
    }

    private fun fakeGraphicsObject(
        id: Int = 1,
        level: Int = 0,
        finished: Boolean = false
    ): GraphicsObject {
        val obj = mockk<GraphicsObject>(relaxed = true)
        every { obj.id } returns id
        every { obj.level } returns level
        every { obj.finished() } returns finished
        return obj
    }

    private fun buildContext(
        graphicsObjects: List<GraphicsObject> = emptyList()
    ) {
        val ctx = fakeContext {
            every { worldViews.getTopLevelGraphicsObjects() } returns graphicsObjects
        }
        ApiContext.init(ctx)
    }

    @Test
    fun `no filters returns all graphics objects`() {
        val objects = listOf(
            fakeGraphicsObject(id = 100),
            fakeGraphicsObject(id = 200),
            fakeGraphicsObject(id = 300)
        )
        buildContext(graphicsObjects = objects)

        val results = GraphicObjectQueryBuilder().results()
        assertEquals(3, results.size)
    }

    @Test
    fun `ids filter matches by id`() {
        val objects = listOf(
            fakeGraphicsObject(id = 100),
            fakeGraphicsObject(id = 200),
            fakeGraphicsObject(id = 300)
        )
        buildContext(graphicsObjects = objects)

        val results = GraphicObjectQueryBuilder().ids(100, 300).results()
        assertEquals(2, results.size)
    }

    @Test
    fun `ids filter excludes non-matching`() {
        val objects = listOf(
            fakeGraphicsObject(id = 100),
            fakeGraphicsObject(id = 200)
        )
        buildContext(graphicsObjects = objects)

        val results = GraphicObjectQueryBuilder().ids(999).results()
        assertEquals(0, results.size)
    }

    @Test
    fun `custom filter works`() {
        val objects = listOf(
            fakeGraphicsObject(id = 100, level = 0),
            fakeGraphicsObject(id = 200, level = 1)
        )
        buildContext(graphicsObjects = objects)

        val results = GraphicObjectQueryBuilder().filter { it.level == 0 }.results()
        assertEquals(1, results.size)
        assertEquals(100, results.first()?.id)
    }
}
