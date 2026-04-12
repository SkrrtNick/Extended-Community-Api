package org.tribot.api.query

import net.runelite.api.TileObject
import org.tribot.automation.script.ScriptContext

/**
 * Fluent query builder for game objects (TileObjects) in the world.
 */
class ObjectQueryBuilder(private val ctx: ScriptContext) : QueryBuilder<TileObject, ObjectQueryBuilder>() {

    fun names(vararg names: String): ObjectQueryBuilder = filter { obj ->
        val def = ctx.definitions.getObject(obj.id)
        def != null && def.name in names
    }

    fun ids(vararg ids: Int): ObjectQueryBuilder = filter { obj ->
        obj.id in ids.toSet()
    }

    fun actions(vararg actions: String): ObjectQueryBuilder = filter { obj ->
        val def = ctx.definitions.getObject(obj.id)
        def != null && def.actions.filterNotNull().any { it in actions.toSet() }
    }

    fun withinDistance(maxDistance: Int): ObjectQueryBuilder = filter { obj ->
        val playerLocation = ctx.worldViews.getLocalPlayer()?.worldLocation ?: return@filter false
        obj.worldLocation.distanceTo(playerLocation) <= maxDistance
    }

    override fun results(): LocatableQueryResults<TileObject> {
        val filtered = applyFilters(fetchEntities())
        return LocatableQueryResults(filtered) { it.worldLocation }
    }

    override fun fetchEntities(): List<TileObject> =
        ctx.worldViews.getTopLevelObjects()
}
