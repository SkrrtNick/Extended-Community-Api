package org.tribot.api.query

import net.runelite.api.GraphicsObject
import org.tribot.automation.script.ScriptContext

/**
 * Fluent query builder for graphics objects (spot animations) in the game world.
 */
class GraphicObjectQueryBuilder(private val ctx: ScriptContext) : QueryBuilder<GraphicsObject, GraphicObjectQueryBuilder>() {

    fun ids(vararg ids: Int): GraphicObjectQueryBuilder = filter { it.id in ids.toSet() }

    override fun fetchEntities(): List<GraphicsObject> = ctx.worldViews.getTopLevelGraphicsObjects()

    override fun results(): QueryResults<GraphicsObject> {
        val filtered = applyFilters(fetchEntities())
        return QueryResults(filtered)
    }
}
