package org.tribot.api.query

import org.tribot.automation.script.ScriptContext
import org.tribot.automation.script.core.GroundItem

/**
 * Fluent query builder for ground items in the game world.
 */
class GroundItemQueryBuilder(private val ctx: ScriptContext) : QueryBuilder<GroundItem, GroundItemQueryBuilder>() {

    fun names(vararg names: String): GroundItemQueryBuilder = filter { item ->
        val def = ctx.definitions.getItem(item.id)
        def != null && def.name in names
    }

    fun ids(vararg ids: Int): GroundItemQueryBuilder = filter { item ->
        item.id in ids.toSet()
    }

    fun minQuantity(min: Int): GroundItemQueryBuilder = filter { item ->
        item.quantity >= min
    }

    fun maxQuantity(max: Int): GroundItemQueryBuilder = filter { item ->
        item.quantity <= max
    }

    fun withinDistance(maxDistance: Int): GroundItemQueryBuilder = filter { item ->
        val playerLocation = ctx.worldViews.getLocalPlayer()?.worldLocation ?: return@filter false
        item.position.distanceTo(playerLocation) <= maxDistance
    }

    override fun results(): LocatableQueryResults<GroundItem> {
        val filtered = applyFilters(fetchEntities())
        return LocatableQueryResults(filtered) { it.position }
    }

    override fun fetchEntities(): List<GroundItem> =
        ctx.worldViews.getTopLevelGroundItems()
}
