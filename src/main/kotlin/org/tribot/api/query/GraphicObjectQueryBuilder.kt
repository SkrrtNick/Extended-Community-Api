package org.tribot.api.query

import net.runelite.api.GraphicsObject
import org.tribot.api.ApiContext

/**
 * Fluent query builder for graphics objects (spot animations) in the game world.
 */
class GraphicObjectQueryBuilder : QueryBuilder<GraphicsObject, GraphicObjectQueryBuilder>() {

    fun ids(vararg ids: Int): GraphicObjectQueryBuilder = filter { it.id in ids.toSet() }

    override fun fetchEntities(): List<GraphicsObject> = ApiContext.get().worldViews.getTopLevelGraphicsObjects()

    override fun results(): QueryResults<GraphicsObject> {
        val filtered = applyFilters(fetchEntities())
        return QueryResults(filtered)
    }
}
